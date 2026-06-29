package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.ScenarioAnalyzerService;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {
    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final ScenarioAnalyzerService scenarioAnalyzerService;

    public void start() {
        try {
            snapshotConsumer.subscribe(Collections.singletonList("telemetry.snapshots.v1"));
            log.info("SnapshotProcessor запущен, подписка на топик telemetry.snapshots.v1 оформлена");

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    log.info("Получен снимок состояния (snapshot) для хаба: {}", record.key());
                    scenarioAnalyzerService.analyze(record.value());
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
}

//package ru.yandex.practicum.config;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.common.errors.WakeupException;
//import org.springframework.beans.factory.SmartInitializingSingleton;
//import org.springframework.beans.factory.annotation.Value;
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
//public class SnapshotProcessor implements SmartInitializingSingleton {
//
//    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
//    private final SnapshotHandler snapshotHandler;
//
//    @Value("${analyzer.topic.snapshots-topic:telemetry.snapshots.v1}")
//    private String snapshotTopic;
//
//    @Override
//    public void afterSingletonsInstantiated() {
//        Thread thread = new Thread(this::runConsumer);
//        thread.setName("SnapshotProcessor-Thread");
//        thread.setDaemon(true);
//        thread.start();
//        log.info("Поток SnapshotProcessor успешно запущен");
//    }
//
//    private void runConsumer() {
//        try {
//            snapshotConsumer.subscribe(Collections.singletonList(snapshotTopic));
//            log.info("SnapshotProcessor успешно подписался на топик {}", snapshotTopic);
//
//            while (!Thread.currentThread().isInterrupted()) {
//                try {
//                    ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofMillis(500));
//
//                    for (var record : records) {
//                        log.info("Получен снапшот из Kafka для хаба (key): {}", record.key());
//                        if (record.value() != null) {
//                            try {
//                                snapshotHandler.handle(record.value());
//                            } catch (Exception e) {
//                                log.error("Ошибка при обработке снапшота хендлером для хаба {}: {}", record.key(), e.getMessage(), e);
//                            }
//                        }
//                    }
//
//                    if (!records.isEmpty()) {
//                        snapshotConsumer.commitSync();
//                    }
//                } catch (WakeupException e) {
//                    log.info("SnapshotProcessor получил сигнал остановки (wakeup).");
//                    break;
//                } catch (Exception e) {
//                    log.error("Ошибка во время итерации poll() в SnapshotProcessor: {}", e.getMessage(), e);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ie) {
//                        Thread.currentThread().interrupt();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Критический сбой инициализации подписки в SnapshotProcessor", e);
//        } finally {
//            try {
//                snapshotConsumer.close();
//            } catch (Exception e) {
//                log.error("Ошибка при закрытии snapshotConsumer: {}", e.getMessage());
//            }
//            log.info("SnapshotProcessor: consumer успешно закрыт.");
//        }
//    }
//}