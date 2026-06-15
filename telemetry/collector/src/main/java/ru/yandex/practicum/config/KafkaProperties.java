package ru.yandex.practicum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    private String bootstrapServers;
    private Topics topics = new Topics();
    private Serializers serializers = new Serializers();

    @Data
    public static class Topics {
        private String hubs;
        private String sensors;
    }

    @Data
    public static class Serializers {
        private String key;
        private String value;
    }
}