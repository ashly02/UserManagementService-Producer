package com.project.usermanagement.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.project.usermanagement.dao.UserDao;
import com.project.usermanagement.dto.UserDTO;
import com.project.usermanagement.response.Response;
import com.project.usermanagement.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@AutoConfigureMockMvc
public class UserControllerTest {
    private static final String SUCCESS = "Success";
    private final UUID userid1 = UUID.randomUUID();
    private final UUID userid2= UUID.randomUUID();
    private final String useridString=userid1.toString();

    @Mock
    UserService userService;
    @Autowired
    MockMvc mockMvc;
    @InjectMocks
    UserController userController;
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testCreate() throws Exception {
        UserDTO userDTO =  new UserDTO(userid1, "ashly", "church street", "ashly@example.com", (short) 25);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(userDTO);

        when(userService.createUser(any(UserDTO.class))).thenReturn(new Response(SUCCESS, userDTO));

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse resultResponse = result.getResponse();
        String responseBody = resultResponse.getContentAsString();
        Response responseObject = mapper.readValue(responseBody, Response.class);

        assertEquals(SUCCESS, responseObject.getStatus());
        assertNull(responseObject.getErrors());
        UserDTO userDTOFromResponse = mapper.convertValue(responseObject.getData(), UserDTO.class);
        assertEquals("ashly", userDTOFromResponse.getUsername());
        assertEquals(userid1,userDTOFromResponse.getUserid());
    }

    @Test
    public void test_read() throws Exception {
        List<UserDTO> userDTOS = Arrays.asList(
                new UserDTO(userid1, "ashly", "church street", "ashly@example.com", (short) 25),
                new UserDTO(userid2, "anjana", "church street", "ashly@example.com", (short) 25)
        );
        Response response = new Response(SUCCESS, userDTOS);
        when(userService.getAllUser()).thenReturn(response);

        MvcResult result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse resultResponse = result.getResponse();
        String responseBody = resultResponse.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Response responseObject = mapper.readValue(responseBody, Response.class);
        assertEquals(SUCCESS, responseObject.getStatus());
        assertNull(responseObject.getErrors());
        List<UserDTO> userDTOList = mapper.convertValue(responseObject.getData(), mapper.getTypeFactory().constructCollectionType(List.class, UserDTO.class));
        assertEquals("ashly", userDTOList.get(0).getUsername());
        assertEquals("anjana", userDTOList.get(1).getUsername());
        assertEquals(userid1,userDTOList.get(0).getUserid());
        assertEquals(userid2,userDTOList.get(1).getUserid());
    }

    @Test
    public void test_readUser() throws Exception {
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street", "ashly@example.com", (short) 25);
        Response response = new Response(SUCCESS, userDTO);
        when(userService.getUser(eq(useridString))).thenReturn(response);

        MvcResult result = mockMvc.perform(get("/users/{userId}",useridString))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse resultResponse = result.getResponse();
        String responseBody = resultResponse.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Response responseObject = mapper.readValue(responseBody, Response.class);
        assertEquals(SUCCESS, responseObject.getStatus());
        assertNull(responseObject.getErrors());
        UserDTO responseUserDTO=mapper.convertValue(responseObject.getData(),UserDTO.class);
        assertEquals("ashly",responseUserDTO.getUsername());
        assertEquals(userid1,responseUserDTO.getUserid());
    }

    @Test
    public void test_updateUser() throws Exception {
        UserDTO userDTO = new UserDTO(userid1, "ashly", "church street", "ashly@example.com", (short) 25);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(userDTO);

        when(userService.updateUser(eq(useridString), any(UserDTO.class))).thenReturn(new Response(SUCCESS, userDTO));

        MvcResult result = mockMvc.perform(patch("/users/{userId}", useridString)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse resultResponse = result.getResponse();
        String responseBody = resultResponse.getContentAsString();

        Response responseObject = mapper.readValue(responseBody, Response.class);

        assertEquals(SUCCESS, responseObject.getStatus());
        assertNull(responseObject.getErrors());
        UserDTO userDTOFromResponse = mapper.convertValue(responseObject.getData(), UserDTO.class);
        assertEquals("ashly", userDTOFromResponse.getUsername());
        assertEquals(userid1,userDTOFromResponse.getUserid());
    }

    @Test
    public void test_deleteUser() throws Exception {
        Response response = new Response(SUCCESS, null);
        when(userService.deleteUser(eq(useridString))).thenReturn(response);

        MvcResult result = mockMvc.perform(delete("/users/{userId}", useridString))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse resultResponse = result.getResponse();
        String responseBody = resultResponse.getContentAsString();

        Response responseObject = new ObjectMapper().readValue(responseBody, Response.class);
        assertNull(responseObject.getData());
        assertNull(responseObject.getErrors());
        assertEquals(SUCCESS, responseObject.getStatus());
    }
}