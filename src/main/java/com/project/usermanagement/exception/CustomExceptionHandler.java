package com.project.usermanagement.exception;

import com.project.usermanagement.response.Response;
import com.project.usermanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {
    private static final Logger logger= LoggerFactory.getLogger(UserService.class);
    private static final String FAILURE ="Failure";
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Response> handleDataAccessExceptions(DataAccessException ex){
        Response errorResponse = new Response(FAILURE,null,"Database connection error");
        logger.error(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public  ResponseEntity<Response> handleResourceNotFoundException(Exception ex){
        Response errorResponse = new Response(FAILURE,null,ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleExceptions(Exception ex){
        Response errorResponse = new Response(FAILURE,null,"Some error occurred");
        logger.error(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


}
