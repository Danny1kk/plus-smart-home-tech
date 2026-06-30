package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.HubEventHandlerFactory;
import ru.yandex.practicum.service.hub.HubEventHandler;
import ru.yandex.practicum.service.snapshot.SnapshotHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerEventsListener {

    private final SnapshotHandler snapshotHandler;
    private final HubEventHandlerFactory handlerFactory;

    @KafkaListener(
            topics = "${analyzer.topic.snapshots-topic:telemetry.snapshots.v1}",
            groupId = "${spring.kafka.consumer.snapshots.group-id}",
            containerFactory = "snapshotListenerContainerFactory"
    )
    public void listenSnapshots(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        log.info("Получен снапшот для хаба из Kafka: {}", record.key());
        if (record.value() != null) {
            snapshotHandler.handle(record.value());
        }
    }

    @KafkaListener(
            topics = "${analyzer.topic.hub-events:telemetry.hubs.v1}",
            groupId = "${spring.kafka.consumer.hub.group-id}",
            containerFactory = "hubListenerContainerFactory"
    )
    public void listenHubEvents(ConsumerRecord<String, HubEventAvro> record) {
        if (record.value() != null) {
            HubEventAvro event = record.value();
            String payloadClassName = event.getPayload().getClass().getSimpleName();
            log.info("Получено событие хаба из Kafka, тип: {}", payloadClassName);

            HubEventHandler handler = handlerFactory.getHubMap().get(payloadClassName);
            if (handler != null) {
                handler.handle(event);
            } else {
                log.warn("Не найден обработчик для события типа: {}", payloadClassName);
            }
        }
    }
}