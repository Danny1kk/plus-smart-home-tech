package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.snapshot.SnapshotHandler;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final SnapshotHandler snapshotHandler;

    @Value("${analyzer.topic.snapshots-topic:telemetry.snapshots.v1}")
    private String snapshotTopic;

    @PostConstruct
    public void start() {
        Thread thread = new Thread(() -> {
            try {
                snapshotConsumer.subscribe(Collections.singletonList(snapshotTopic));
                log.info("SnapshotProcessor запущен, подписка на топик {}", snapshotTopic);

                while (!Thread.currentThread().isInterrupted()) {
                    ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofSeconds(1));
                    for (var record : records) {
                        log.info("Получен снапшот для хаба: {}", record.key());
                        if (record.value() != null) {
                            snapshotHandler.handle(record.value());
                        }
                    }
                    if (!records.isEmpty()) {
                        snapshotConsumer.commitSync();
                    }
                }
            } catch (WakeupException e) {
                log.info("SnapshotProcessor получил сигнал остановки (wakeup).");
            } catch (Exception e) {
                log.error("Критическая ошибка в SnapshotProcessor", e);
            } finally {
                try {
                    snapshotConsumer.close();
                } catch (Exception e) {
                    log.error("Ошибка при закрытии snapshotConsumer", e);
                }
                log.info("SnapshotProcessor: consumer закрыт.");
            }
        });
        thread.setName("SnapshotProcessor-Thread");
        thread.start();
        log.info("Поток SnapshotProcessor успешно запущен");
    }
}