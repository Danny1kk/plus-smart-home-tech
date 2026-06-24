package ru.yandex.practicum.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.snapshot.SnapshotHandler;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {
    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final SnapshotHandler snapshotHandler;

    @Override
    public void run() {
        try {
            snapshotConsumer.subscribe(Collections.singletonList("telemetry.snapshots.v1"));
            log.info("SnapshotProcessor запущен, подписка на топик telemetry.snapshots.v1 оформлена");

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    log.info("Получен снимок состояния (snapshot) для хаба: {}", record.key());
                    snapshotHandler.handle(record.value());
                }
                if (!records.isEmpty()) {
                    snapshotConsumer.commitSync();
                }
            }
        } catch (WakeupException e) {
            log.info("SnapshotProcessor получил сигнал остановки (wakeup).");
        } finally {
            snapshotConsumer.close();
        }
    }

    private final Thread thread = new Thread(this);

    @PostConstruct
    public void start() {
        thread.start();
        log.info("Поток SnapshotProcessor запущен через @PostConstruct");
    }
}