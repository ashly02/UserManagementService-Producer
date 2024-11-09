package com.project.usermanagement.util;

import com.project.usermanagement.exception.ResourceNotFoundException;
import com.project.usermanagement.model.User;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RedisUtilTest {

    @Mock
    RedisTemplate<String,Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RedisOperations<String, Object> redisOperations;

    @Mock
    ScanOptions scanOptions;

    @InjectMocks
    RedisUtil redisUtil;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStoreDataInCache_valid(){
        String key = "test_key";
        Object data = new Object();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisUtil.storeDataInCache(key, data);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(key, data);
        verify(redisTemplate).expire(key, Duration.ofMinutes(3));

    }
    @Test
    public void testStoreDataInCache_Exception() {
        String key = "test_key";
        Object data = new Object();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Error")).when(valueOperations).set(key, data);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            redisUtil.storeDataInCache(key, data);
        });

        assertEquals("Error storing data in Redis cache",exception.getMessage());
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(key, data);
        verify(redisTemplate, never()).expire(any(), any());
    }

    @Test
    public void testGetCachedData_notFound() {
        // Arrange
        String key = "nonExistentKey";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);
        // Act
        Object result = redisUtil.getCachedData(key);

        // Assert
        assertNull(result);
        verify(redisTemplate.opsForValue()).get(key);
    }

    @Test
    public void testGetCachedData_found() {
        // Arrange
        String key = "testKey";
        Object expectedData = new User(UUID.randomUUID(), "ashly", "church street","ashly@gmail.com", (short) 25);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(expectedData);
        Object result = redisUtil.getCachedData(key);

        assertNotNull(result);
        assertEquals(expectedData, result);
        verify(redisTemplate.opsForValue()).get(key);
    }
    @Test
    public void testGetCachedData_exception() {
        // Arrange
        String key = "testKey";
        Object expectedData = new User(UUID.randomUUID(), "ashly", "church street","ashly@gmail.com", (short) 25);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis Error"));
        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            redisUtil.getCachedData(key);
        });

        assertEquals("Error retrieving data from Redis cache", thrown.getMessage());
        verify(redisTemplate.opsForValue()).get(key);
    }

    @Test
    public void testDeleteCachedData_userFound() {
        // Arrange
        String key = "testKey";
        Object expectedData = new User(UUID.randomUUID(), "ashly", "church street","ashly@gmail.com", (short) 25);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(key)).thenReturn(expectedData);
        // Act & Assert
        Object data =redisUtil.deleteCachedData(key);
        assertEquals(expectedData,data);
        verify(redisTemplate.opsForValue()).getAndDelete(key);
    }

    @Test
    public void testDeleteCachedData_userNotFound() {
        // Arrange
        String key = "testKey";
        Object expectedData = new User(UUID.randomUUID(), "ashly", "church street","ashly@gmail.com", (short) 25);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(key)).thenReturn(null);
        // Act & Assert
        Object data =redisUtil.deleteCachedData(key);
        assertNull(data);
        verify(redisTemplate.opsForValue()).getAndDelete(key);
    }

    @Test
    public void testDeleteCachedData_Exception() {
        // Arrange
        String key = "testKey";
        Object expectedData = new User(UUID.randomUUID(), "ashly", "church street","ashly@gmail.com", (short) 25);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(key)).thenThrow(new RuntimeException("Error deleting data from Redis cache"));
        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            redisUtil.deleteCachedData(key);
        });

        assertEquals("Error deleting data from Redis cache",thrown.getMessage());

        verify(redisTemplate.opsForValue()).getAndDelete(key);
    }

}
