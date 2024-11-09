package com.project.usermanagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configurable
@EnableKafka
public class KafkaConfig {
    @Value("${spring.kafka.producer.bootstrap-servers:localhost:9092,localhost:9093,localhost:9094}")
    private String bootstrapServers;

    @Value("${spring.kafka.topic}")
    public String topic;

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(topic).partitions(3).replicas(3).build();
    }

    @Bean
    public ProducerFactory<String, String> producerStringFactory() {
        Map<String,Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.ACKS_CONFIG,"all");
        configProps.put(ProducerConfig.RETRIES_CONFIG,3);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerStringFactory());
    }
}
