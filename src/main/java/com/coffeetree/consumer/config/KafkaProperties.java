package com.coffeetree.consumer.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kafka")
@Data
public class KafkaProperties {
    public static final String CONSUMER_GROUP = "group";
    public static final String TOPIC = "";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
}
