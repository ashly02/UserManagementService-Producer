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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    private static final String SUCCESS = "Success";
    private static final String FAILURE = "Failure";
    private static final String ERROR1 = "Username must be at least 3 characters";
    private static final String ERROR2="Invalid email format";
    private static final String userId="invalid userid";
    private final UUID userid1 = UUID.randomUUID();
    private final UUID userid2 = UUID.randomUUID();
    private final String useridString = userid1.toString();

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Value("${app.message}")
    private String type;

    @Mock
    private KafkaProducerUtil userManagementProducer;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private ModelMapper modelMapper;

    @Test
    public void testCreateUser_validUser() throws JsonProcessingException {
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        User user = new User(userid1, "ashly", "church street","ashly@gmail.com", (short) 25);
//        Message kafkaMessage=new Message(useridString, System.currentTimeMillis(),"Create");
//        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        System.out.println(type);
        when(userDao.save(any(User.class))).thenReturn(user);

        Response response = userService.createUser(userDTO);

        assertEquals(SUCCESS, response.getStatus());
        assertEquals(user, response.getData());
        assertNull(response.getErrors());
    }

    @Test
    public void testCreateUser_invalidUser_usernameNull() throws JsonProcessingException {

        UserDTO userDTO = new UserDTO(userid1, null, "church street","ashly@example.com", (short) 25);
        Message kafkaMessage=new Message(useridString,System.currentTimeMillis(),"Create");
//        when(userManagementProducer.sendKafkaMessage(kafkaMessage)).thenReturn(null);
        Response response = userService.createUser(userDTO);

        assertEquals(FAILURE, response.getStatus());
        assertEquals(ERROR1,response.getErrors());
        assertNull(response.getData());
    }
    @Test
    public void testCreateUser_invalidUser_usernameShort() throws JsonProcessingException {
        UserDTO userDTO = new UserDTO(userid1, "xx", "church street","ashly@example.com", (short) 25);
        Message kafkaMessage=new Message(useridString,System.currentTimeMillis(),"Create");
        Response response = userService.createUser(userDTO);
        assertEquals(FAILURE, response.getStatus());
        assertEquals(ERROR1,response.getErrors());
        assertNull(response.getData());
    }

    @Test
    public void testCreateUser_invalidUser_invalidEmail() throws JsonProcessingException {
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street","xxx", (short) 25);
        Message kafkaMessage=new Message(useridString,System.currentTimeMillis(),"Create");
//        when(userManagementProducer.sendKafkaMessage(kafkaMessage)).thenReturn(null);
        Response response = userService.createUser(userDTO);
        assertEquals(FAILURE, response.getStatus());
        assertEquals(ERROR2,response.getErrors());
        assertNull(response.getData());
    }

    @Test
    public void testGetAllUser() {
        List<User> users = new ArrayList<>();
        users.add(new User(userid1, "ashly", "church street","ashly@example.com", (short) 25));
        users.add(new User(userid2, "ashly", "church street","ashly@example.com", (short) 25));

        when(userDao.findAll()).thenReturn(users);
        System.out.println(users);
        UserDTO userDTO1 = new UserDTO(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        UserDTO userDTO2 = new UserDTO(userid2, "ashly", "church street","ashly@example.com", (short) 25);
        when(modelMapper.map(users.get(0), UserDTO.class)).thenReturn(userDTO1);
        when(modelMapper.map(users.get(1), UserDTO.class)).thenReturn(userDTO2);
        Response response = userService.getAllUser();

        assertNull(response.getErrors());
        assertEquals(SUCCESS, response.getStatus());

        List<UserDTO> userDTOs = (List<UserDTO>) response.getData();
        assertEquals(userDTO1, userDTOs.get(0));
        assertEquals(userDTO2, userDTOs.get(1));
        assertNull(response.getErrors());
        assertEquals(SUCCESS,response.getStatus());
    }

    @Test
    public void testGetUser_CachedUserFound() {

        User user = new User(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        String key = "User::" + useridString;
        when(redisUtil.getCachedData(key)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        Response response = userService.getUser(useridString);
        assertNull(response.getErrors());
        assertEquals(SUCCESS, response.getStatus());
        assertEquals(userDTO, response.getData());
        Mockito.verify(userDao, never()).findById(any());
    }
    @Test
    public void testGetUser_UserFoundInDatabase() {

        User user = new User(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        String key = "User::" + useridString;
        when(redisUtil.getCachedData(key)).thenReturn(null);
        when(userDao.findById(userid1)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        Response response = userService.getUser(useridString);
        assertNull(response.getErrors());
        assertEquals(SUCCESS, response.getStatus());
        assertEquals(userDTO, response.getData());
        Mockito.verify(redisUtil).getCachedData(key);
        Mockito.verify(redisUtil).storeDataInCache(key, user);
    }

    @Test
    public void testGetUser_InvalidUserId() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.getUser(userId));
        assertEquals("Invalid user id "+userId, exception.getMessage());
    }

    @Test
    public void testGetUser_userNotFound() {
        String key = "User::" + useridString;
        when(redisUtil.getCachedData(key)).thenReturn(null);
        when(userDao.findById(userid1)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.getUser(useridString));
        assertEquals("User "+userid1+" not found ",exception.getMessage());
    }

    @Test
    public void testUpdateUser_validUser() throws JsonProcessingException {

        UserDTO userDTO = new UserDTO(userid1, "ashly1", "church street1","ashly1@example.com", (short) 25);

        User user = new User(userid1, "ashly", "church street","ashly@example.com", (short) 25);

        when(userDao.findById(userid1)).thenReturn(Optional.of(user));
        user.setUserid(userid1);
        user.setUsername(userDTO.getUsername());
        user.setAge(userDTO.getAge());
        user.setEmail(userDTO.getEmail());
        user.setAddress(userDTO.getAddress());
        when(userDao.save(user)).thenReturn(user);

        Response response = userService.updateUser(useridString, userDTO);

        assertEquals(SUCCESS, response.getStatus());
        assertEquals(user, response.getData());
        assertNull(response.getErrors());
    }

    @Test
    public void testUpdateUser_InvalidUserId() {
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userId,userDTO));
        assertEquals("Invalid user id "+userId, exception.getMessage());
    }

    @Test
    public void testUpdateUser_usernameNull() throws JsonProcessingException {
        UserDTO userDTO = new UserDTO(userid1, "", "church street","ashly@example.com", (short) 25);
        Response response= userService.updateUser(useridString,userDTO);
        assertEquals(FAILURE,response.getStatus());
        assertEquals("Username must be at least 3 characters",response.getErrors());
        assertNull(response.getData());
    }

    @Test
    public void testUpdateUser_usernameShort() throws JsonProcessingException {
        UserDTO userDTO = new UserDTO(userid1, "xx", "church street","ashly@example.com", (short) 25);
        Response response= userService.updateUser(useridString,userDTO);
        assertEquals(FAILURE,response.getStatus());
        assertEquals("Username must be at least 3 characters",response.getErrors());
        assertNull(response.getData());
    }

    @Test
    public void testUpdateUser_emailInvalid() throws JsonProcessingException {
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street","xxx", (short) 25);
        Response response= userService.updateUser(useridString,userDTO);
        assertEquals(FAILURE,response.getStatus());
        assertEquals("Invalid email format",response.getErrors());
        assertNull(response.getData());
    }

    @Test
    public void testUpdateUser_userNotFound(){
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        when(userDao.findById(userid1)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(useridString,userDTO));
        assertEquals("User " + userid1 + " not found ", exception.getMessage());
    }

    @Test
    public void testDeleteUser_Success() throws JsonProcessingException {
        User user = new User(userid1, "ashly", "church street","ashly@example.com", (short) 25);

        when(userDao.findById(userid1)).thenReturn(Optional.of(user));

        Response response=userService.deleteUser(useridString);
        assertEquals(SUCCESS,response.getStatus());
        assertNull(response.getErrors());
        assertNull(response.getData());
    }

    @Test
    public void testDeleteUser_DataInCache() throws JsonProcessingException {
        User user = new User(userid1, "ashly", "church street","ashly@example.com", (short) 25);
        String key = "User::" + useridString;
        when(userDao.findById(userid1)).thenReturn(Optional.of(user));
        Response response=userService.deleteUser(useridString);
        assertEquals(SUCCESS,response.getStatus());
        assertNull(response.getErrors());
        assertNull(response.getData());
    }

    @Test
    public void testDeleteUser_invalidUserid(){
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));
        assertEquals("Invalid user id"+userId, exception.getMessage());
    }

    @Test
    public  void testDeleteUser_userNotFound(){
        when(userDao.findById(userid1)).thenReturn(Optional.empty());
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,() -> userService.deleteUser(useridString));
        assertEquals("User "+userid1+" not found when deleting",exception.getMessage());
    }
}
