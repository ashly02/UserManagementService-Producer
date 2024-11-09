package com.project.usermanagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.usermanagement.dao.UserDao;
import com.project.usermanagement.exception.ResourceNotFoundException;
import com.project.usermanagement.model.User;
import com.project.usermanagement.dto.UserDTO;
import com.project.usermanagement.util.KafkaProducerUtil;
import com.project.usermanagement.response.Message;
import com.project.usermanagement.response.Response;
import com.project.usermanagement.util.RedisUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserDao userDao;
    private final ModelMapper modelMapper;
    private final KafkaProducerUtil kafkaProducerUtil;
    private final RedisUtil redisUtil;
    private static final String SUCCESS = "Success";
    private static final String FAILURE = "Failure";
    private static final String ERROR1 = "Username must be at least 3 characters";
    private static final String ERROR2 = "Invalid email format";
    private static final String CREATE = "Create";
    private static final String UPDATE = "Update";
    private static final String DELETE = "Delete";
    private static final String KEY_PREFIX="User::";
    private static final Logger logger= LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserDao userDao, ModelMapper modelMapper, KafkaProducerUtil userManagementProducer, RedisUtil redisUtil) {
        this.userDao = userDao;
        this.modelMapper=modelMapper;
        this.kafkaProducerUtil = userManagementProducer;
        this.redisUtil=redisUtil;
    }

    public Response createUser(UserDTO userDTO) throws JsonProcessingException {
        String errorMessage = validateUser(userDTO);
        if (errorMessage != null) {
            logger.error("User cannot be created. {}", errorMessage);
            return new Response(FAILURE, null, errorMessage);
        }
//        User user = modelMapper.map(userDTO, User.class);
        User user = new User();
        UUID userId= UUID.randomUUID();
        user.setUserid(userId);
        user.setAddress(userDTO.getAddress());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());
        user.setUsername(userDTO.getUsername());
        User createdUser = userDao.save(user);
        logger.info("User {} created successfully",userId);
        Message kafkaMessage=new Message(userId.toString(), System.currentTimeMillis(),CREATE);
        kafkaProducerUtil.sendKafkaMessage(kafkaMessage);
        return new Response(SUCCESS,createdUser);

    }

    public Response getAllUser(){
        List<User> users = new ArrayList<>(userDao.findAll());
        List<UserDTO> userDTOs = users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
        return new Response(SUCCESS,userDTOs);
    }

    public Response getUser(String userId){
        if (!isValidUUID(userId)) {
            logger.error("Invalid user id {}.Cannot fetch user", userId);
            throw new ResourceNotFoundException("Invalid user id "+userId);
        }
        UUID uuid = UUID.fromString(userId);
        String key = KEY_PREFIX + userId;
        User user=getCachedOrDBUser(uuid,key);
        UserDTO userDTO=modelMapper.map(user,UserDTO.class);
        return new Response(SUCCESS,userDTO);
    }

    public Response updateUser(String userId, UserDTO userDTO) throws JsonProcessingException {
        if (!isValidUUID(userId)) {
            logger.error("Invalid user id {}.Cannot update user", userId);
            throw new ResourceNotFoundException("Invalid user id "+userId);
        }
        String errorMessage = validateUser(userDTO);
        if (errorMessage!= null) {
            logger.error("User cannot be updated. {}", errorMessage);
            return new Response(FAILURE, null, errorMessage);
        }
        UUID uuid = UUID.fromString(userId);
        String key = KEY_PREFIX + userId;
        User user = getCachedOrDBUser(uuid,key);
        user.setUsername(userDTO.getUsername());
        user.setAddress(userDTO.getAddress());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());
        user.setUserid(UUID.fromString(userId));
        User savedUser = userDao.save(user);
        redisUtil.storeDataInCache(key,savedUser);
        Message kafkaMessage=new Message(userId, System.currentTimeMillis(),UPDATE);
        kafkaProducerUtil.sendKafkaMessage(kafkaMessage);
        logger.info("User updated {} successfully", userId);
        return new Response(SUCCESS, savedUser);
    }

    private User getCachedOrDBUser(UUID uuid, String key) {
        User cached=(User)redisUtil.getCachedData(key);
        if(cached !=null){
            logger.info("Retrieved user from CACHE : {}",cached);
            return cached;
        }
        else{
            logger.info("Retrieving user : {} from database as it is not available in cache",uuid);
            Optional<User> userOptional=userDao.findById(uuid);
            if(userOptional.isPresent()){
                User user=userOptional.get();
                redisUtil.storeDataInCache(key,user);//in update-one
                return user;
            }
            logger.error("User{} not found ", uuid);
            throw new ResourceNotFoundException("User "+uuid+" not found ");
        }
    }

    public Response deleteUser(String userId) throws JsonProcessingException {
        if (!isValidUUID(userId)) {
            logger.error("Invalid user id {}.Cannot delete user.", userId);
            throw new ResourceNotFoundException("Invalid user id"+userId);
        }
        UUID uuid = UUID.fromString(userId);
        String key = KEY_PREFIX + userId;
        redisUtil.deleteCachedData(key);
        deleteUserFromDatabaseIfExists(uuid);
        Message kafkaMessage=new Message(userId, System.currentTimeMillis(),DELETE);
        kafkaProducerUtil.sendKafkaMessage(kafkaMessage);
        logger.info("User deleted {} successfully",uuid);
        return new Response(SUCCESS,null);
    }
    private void deleteUserFromDatabaseIfExists(UUID uuid) {
        Optional<User> userOptional = userDao.findById(uuid);
        if (userOptional.isPresent()) {
            userDao.deleteById(uuid);
            logger.info("User {} deleted from database.", uuid);

        }
        else {
            logger.error("User{} not found when deleting", uuid);
            throw new ResourceNotFoundException("User " + uuid + " not found when deleting");
        }
    }

    private String validateUser(UserDTO user) {
        if((user.getUsername() == null || user.getUsername().length() < 3) && !isValidEmail(user.getEmail()) ){
            System.out.println(user.getUsername());
            System.out.println(user.getEmail());
            return ERROR1+" " +ERROR2;
        }
        if (user.getUsername() == null || user.getUsername().length() < 3) {
            return ERROR1;
        }
        if (!isValidEmail(user.getEmail())) {
            return ERROR2;
        }
        return null;
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile("^[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                .matcher(email)
                .matches();
    }
    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }

    }
}
