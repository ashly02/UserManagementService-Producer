package com.project.usermanagement.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.usermanagement.response.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerUtil {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerUtil.class);
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaProducerUtil(ObjectMapper objectMapper, KafkaTemplate<String, String> stringKafkaTemplate) {
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.objectMapper = objectMapper;

    }

    public void sendKafkaMessage(Message kafkaMessage) throws JsonProcessingException {
        String key = kafkaMessage.getUserId();
        String value = objectMapper.writeValueAsString(kafkaMessage);
        System.out.println(value);
        System.out.println(kafkaMessage);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("usermanagement-events1", key, value);
        System.out.println(producerRecord);
        stringKafkaTemplate.send(producerRecord);
        log.info("Published message:{}", producerRecord);
    }
}