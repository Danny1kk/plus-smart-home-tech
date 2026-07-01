package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final Environment environment;

    @Bean
    public ConsumerFactory<String, SensorsSnapshotAvro> snapshotConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.bootstrap-servers"));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.snapshots.key-deserializer"));
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.snapshots.value-deserializer"));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.snapshots.group-id"));
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, environment.getProperty("spring.kafka.consumer.snapshots.enable-auto-commit"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put("schema.registry.url", environment.getProperty("spring.kafka.properties.schema.registry.url", "http://localhost:8081"));
        config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SensorsSnapshotAvro> snapshotListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SensorsSnapshotAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(snapshotConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, HubEventAvro> hubEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.bootstrap-servers"));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.hub.key-deserializer"));
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.hub.value-deserializer"));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.hub.group-id"));
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, environment.getProperty("spring.kafka.consumer.hub.enable-auto-commit"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put("schema.registry.url", environment.getProperty("spring.kafka.properties.schema.registry.url", "http://localhost:8081"));
        config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, HubEventAvro> hubListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, HubEventAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(hubEventConsumerFactory());
        return factory;
    }
}