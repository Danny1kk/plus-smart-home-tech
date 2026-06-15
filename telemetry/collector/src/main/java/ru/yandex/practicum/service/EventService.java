package ru.yandex.practicum.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.mapper.SensorEventMapper;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

@Service
public class EventService {
    private final KafkaProperties kafkaProperties;
    private final Producer<String, SpecificRecordBase> producer;

    public EventService(Producer<String, SpecificRecordBase> producer, KafkaProperties properties) {
        this.producer = producer;
        this.kafkaProperties = properties;
    }

    public void createSensorEvent(SensorEvent event) {
        SensorEventAvro sensorEventAvro = SensorEventMapper.toSensorEventAvro(event);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                kafkaProperties.getTopics().getSensors(),
                null,
                event.getTimestamp().toEpochMilli(),
                event.getHubId(),
                sensorEventAvro
        );
        producer.send(record);
    }

    public void createHubEvent(HubEvent event) {
        HubEventAvro hubEventAvro = HubEventMapper.toHubEventAvro(event);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                kafkaProperties.getTopics().getHubs(),
                null,
                event.getTimestamp().toEpochMilli(),
                event.getHubId(),
                hubEventAvro
        );
        producer.send(record);
    }
}
