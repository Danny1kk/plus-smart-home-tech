//package ru.yandex.practicum.config;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.common.errors.WakeupException;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
//import ru.yandex.practicum.service.snapshot.SnapshotHandler;
//
//import java.time.Duration;
//import java.util.Collections;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class SnapshotProcessor {
//    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
//    private final SnapshotHandler snapshotHandler;
//
//    @PostConstruct
//    public void start() {
//
//        Thread thread = new Thread(() -> {
//            try {
//                snapshotConsumer.subscribe(Collections.singletonList("telemetry.snapshots.v1"));
//                log.info("SnapshotProcessor запущен, подписка на топик telemetry.snapshots.v1 оформлена");
//
//                while (true) {
//                    ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofSeconds(1));
//                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
//                        log.info("Получен снимок состояния (snapshot) для хаба: {}", record.key());
//                        snapshotHandler.handle(record.value());
//                }
//                if (!records.isEmpty()) {
//                    snapshotConsumer.commitSync();
//                }
//            }
//        } catch (WakeupException e) {
//            log.info("SnapshotProcessor получил сигнал остановки (wakeup).");
//        } finally {
//            snapshotConsumer.close();
//        }
//
//    });
//
//    thread.setName("SnapshotProcessor");
//
//    thread.start();
//
//    log.info("SnapshotProcessor запущен");
//    }
//}

package ru.yandex.practicum.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.snapshot.SnapshotHandler;

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
                log.info("SnapshotProcessor запущен, подписка на топик {} оформлена", snapshotTopic);

                while (true) {
                    ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofSeconds(1));

                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                        log.info("=== [Analyzer] Получен снимок состояния (snapshot) для хаба: {} ===", record.key());

                        snapshotHandler.handle(record.value());

                        log.info("=== [Analyzer] Снимок хаба {} успешно обработан ===", record.key());
                    }

                    if (!records.isEmpty()) {
                        log.info("[Analyzer] Коммичу оффсеты для {} записей", records.count());
                        snapshotConsumer.commitSync();
                    }
                }
            } catch (WakeupException e) {
                log.info("SnapshotProcessor получил сигнал остановки (wakeup).");
            } catch (Exception e) {
                log.error("[Analyzer] Критическая ошибка в потоке SnapshotProcessor: ", e);
            } finally {
                snapshotConsumer.close();
                log.info("SnapshotProcessor: snapshotConsumer успешно закрыт.");
            }
        });

        thread.setName("SnapshotProcessor-Thread");
        thread.start();

        log.info("Поток SnapshotProcessor успешно инициализирован и запущен в фоне");
    }
}