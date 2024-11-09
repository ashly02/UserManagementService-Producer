package com.project.usermanagement.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.usermanagement.response.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KafkaProducerUtilTest {

    @Mock
    ObjectMapper obj;

    @Mock
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @InjectMocks
    private KafkaProducerUtil kafkaProducerUtil;
    @BeforeEach
    void setUp() {
    }

    @Test
    public void testSendKafkaMessage() throws JsonProcessingException {
        // Arrange
        String key=UUID.randomUUID().toString();
        Message kafkaMessage = new Message(key,System.currentTimeMillis(),"Create");
        ObjectMapper objectMapper =new ObjectMapper();
        String value = objectMapper.writeValueAsString(kafkaMessage);

        when(obj.writeValueAsString(kafkaMessage)).thenReturn(value);

        ListenableFuture<SendResult<String, String>> future = mock(ListenableFuture.class);
        when(stringKafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        kafkaProducerUtil.sendKafkaMessage(kafkaMessage);

        verify(obj).writeValueAsString(kafkaMessage);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(stringKafkaTemplate).send(captor.capture());

        ProducerRecord<String, String> sentRecord = captor.getValue();
        assertEquals("usermanagement-events1", sentRecord.topic());
        assertEquals(key, sentRecord.key());
        assertEquals(value, sentRecord.value());
    }
}
