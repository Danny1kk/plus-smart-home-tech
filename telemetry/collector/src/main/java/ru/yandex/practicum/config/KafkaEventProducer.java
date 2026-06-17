package ru.yandex.practicum.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.errorHandler.KafkaSendException;

@Slf4j
@Component
public class KafkaEventProducer implements DisposableBean {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaEventProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRecord(ProducerParam param) {
        if (!param.isValid()) {
            throw new IllegalArgumentException("Недопустимый ProducerParam: " + param);
        }

        byte[] value = param.getValue();
        log.info("ОТПРАВКА: Топик={}, Ключ={}, Размер={}. Первые 4 байта: [{}, {}, {}, {}]",
                param.getTopic(), param.getKey(), value.length,
                value[0], value[1], value[2], value[3]);

        try {
            ProducerRecord<String, byte[]> record = createProducerRecord(param);
            sendKafkaMessage(record);
        } catch (Exception e) {
            handleException(param, e);
        }
    }

    private ProducerRecord<String, byte[]> createProducerRecord(ProducerParam param) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                param.getTopic(),
                param.getPartition(),
                param.getTimestamp(),
                param.getKey(),
                param.getValue()
        );

        if (param.getEventClass() != null) {
            record.headers().add("event_class", param.getEventClass().getBytes());
        }
        if (param.getEventType() != null) {
            record.headers().add("event_type", param.getEventType().getBytes());
        }

        record.headers().add("content-type", "application/x-protobuf".getBytes());
        record.headers().add("ce-type", param.getEventClass() != null ? param.getEventClass().getBytes() : "unknown".getBytes());
        record.headers().add("ce-source", "telemetry-collector".getBytes());
        record.headers().add("ce-specversion", "1.0".getBytes());

        return record;
    }

    private void sendKafkaMessage(ProducerRecord<String, byte[]> record) throws Exception {
        try {
            byte[] data = record.value();
            log.info("ОТПРАВКА В KAFKA: топик={}, ключ={}, размер={} байт",
                    record.topic(), record.key(), data != null ? data.length : 0);

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Ошибка асинхронной отправки в Kafka для топика {}", record.topic(), ex);
                } else {
                    log.info("Успешно отправлено в Kafka! Смещение: {}", result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            throw new KafkaSendException("Ошибка при отправке сообщения", e);
        }
    }

    private void handleException(ProducerParam param, Exception e) {
        log.error("Ошибка при отправке сообщения для param={}", param, e);
    }

    @Override
    public void destroy() throws Exception {
        try {
            kafkaTemplate.flush();
            log.info("KafkaEventProducer корректно остановлен");
        } catch (Exception e) {
            log.error("Ошибка при закрытии KafkaEventProducer", e);
            throw e;
        }
    }
}