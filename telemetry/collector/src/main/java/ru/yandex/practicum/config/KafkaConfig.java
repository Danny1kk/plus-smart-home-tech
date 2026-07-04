package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final Environment environment;

    @Bean
    public Producer<String, SpecificRecordBase> getProducer() {
        Properties config = new Properties();

        String bootstrapServers = environment.getProperty("spring.producer.bootstrap-servers",
                environment.getProperty("spring.kafka.producer.bootstrap-servers", "localhost:9092"));

        String keySerializer = environment.getProperty("spring.producer.key-serializer",
                environment.getProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer"));

        String valueSerializer = environment.getProperty("spring.producer.value-serializer",
                environment.getProperty("spring.kafka.producer.value-serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer"));

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        return new KafkaProducer<>(config);
    }
}