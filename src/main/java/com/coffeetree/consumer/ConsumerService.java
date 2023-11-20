package com.coffeetree.consumer;

import com.coffeetree.consumer.config.KafkaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ConsumerService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = KafkaProperties.TOPIC, groupId = KafkaProperties.CONSUMER_GROUP)
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received message : {}", record.value());
        String base64String = record.value().replaceAll("\"", "");
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);
        try {
            // JSON 문자열을 Container 객체로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            Container container = objectMapper.readValue(jsonString, Container.class);
            log.info("id : {}", container.getId());
            log.info("x : {}", container.getX());
            log.info("y : {}", container.getY());
            simpMessagingTemplate.convertAndSend("/topic/container", container);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
