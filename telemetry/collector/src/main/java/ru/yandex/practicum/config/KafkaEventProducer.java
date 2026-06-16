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
        try {
            ProducerRecord<String, byte[]> record = createProducerRecord(param);
            sendKafkaMessage(record);
        } catch (Exception e) {
            handleException(param, e);
        }
    }

    private ProducerRecord<String, byte[]> createProducerRecord(ProducerParam param) {
        return new ProducerRecord<>(
                param.getTopic(),
                param.getPartition(),
                param.getTimestamp(),
                param.getKey(),
                param.getValue()
        );
    }

    private void sendKafkaMessage(ProducerRecord<String, byte[]> record) throws Exception {
        try {
            kafkaTemplate.send(record).get();
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