package com.project.usermanagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.usermanagement.dto.UserDTO;
import com.project.usermanagement.response.Response;
import com.project.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //create user
    @PostMapping
    @Operation(summary = "Create a new user" , description = "Creates a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "User created successfully"),
            @ApiResponse(responseCode = "400" , description = "Invalid request")
    })
    public ResponseEntity<Response> create(@RequestBody UserDTO user) throws JsonProcessingException {
        Response response = userService.createUser(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //fetch all users
    @GetMapping
    @Operation(summary = "Get all users" , description = "Fetch all the existing users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Users fetched successfully"),
            @ApiResponse(responseCode = "400" , description = "Invalid request")    })
    public ResponseEntity<Response> read(){
        Response response=userService.getAllUser();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //fetch a single user
    @GetMapping("/{userId}")
    @Operation(summary = "Get user" , description = "Get user with the specific userId if it exist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "User fetched successfully"),
            @ApiResponse(responseCode = "400" , description = "Invalid request")    })
    public ResponseEntity<Response> readUser(@PathVariable String userId){
        Response response = userService.getUser(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //update User
    @PatchMapping("/{userId}")
    @Operation(summary = "Update user" , description = "Update user with the specific userId if it exist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "User updated successfully"),
            @ApiResponse(responseCode = "400" , description = "Invalid request")    })
    public ResponseEntity<Response> update(@PathVariable String userId, @RequestBody UserDTO userDTO) throws JsonProcessingException {
        Response response= userService.updateUser(userId,userDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //delete User
    @DeleteMapping("/{userId}")
    @Operation(summary = "delete user" , description = "Deletes user with the specific userId if it exist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "User deleted successfully"),
            @ApiResponse(responseCode = "400" , description = "Invalid request")    })
    public ResponseEntity<Response> delete(@PathVariable String userId) throws JsonProcessingException {
        Response response = userService.deleteUser(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
