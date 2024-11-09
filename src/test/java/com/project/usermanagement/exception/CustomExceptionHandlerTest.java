package com.project.usermanagement.exception;

import com.project.usermanagement.response.Response;
import com.project.usermanagement.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class CustomExceptionHandlerTest {
    private static final String FAILURE = "Failure";
    private final UUID userid=UUID.randomUUID();
    private final String useridString=userid.toString();
    @Mock
    private UserService userService;
    @InjectMocks
    private CustomExceptionHandler customExceptionHandler;

    @Test
    public void test_ResourceNotFoundException() {
        Exception ex = new ResourceNotFoundException("User not found");
        ResponseEntity<Response> response = customExceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Response errorResponse = response.getBody();
        assert errorResponse != null;
        assertNull(errorResponse.getData());
        assertEquals(FAILURE, errorResponse.getStatus());
        assertEquals("User not found",errorResponse.getErrors());
    }
    @Test
    public void test_Exception() {
        Exception ex = new RuntimeException("Some error occurred");

        ResponseEntity<Response> response = customExceptionHandler.handleExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Response errorResponse = response.getBody();
        assert errorResponse != null;
        assertEquals(FAILURE, errorResponse.getStatus());
        assertNull(errorResponse.getData());
        assertEquals("Some error occurred",errorResponse.getErrors());
    }

    @Test
    public void test_DataAccessException() {
        DataAccessException dataAccessException = mock(DataAccessException.class); //can't be instantiated hence mocked
        when(dataAccessException.getMessage()).thenReturn("Database connection error");
        lenient().when(userService.getUser(useridString)).thenThrow(dataAccessException);

        ResponseEntity<Response> response = customExceptionHandler.handleDataAccessExceptions(dataAccessException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Response errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNull(errorResponse.getData());
        assertEquals(FAILURE, errorResponse.getStatus());
        assertEquals("Database connection error",errorResponse.getErrors());
    }
}
