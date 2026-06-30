package ru.yandex.practicum.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.HubEventHandlerFactory;
import ru.yandex.practicum.service.hub.HubEventHandler;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final HubEventHandlerFactory handlerFactory;

    @Value("${analyzer.topic.hub-events:telemetry.hubs.v1}")
    private String hubEventsTopic;

    @Override
    public void run() {
        try {
            hubEventConsumer.subscribe(Collections.singletonList(hubEventsTopic));
            log.info("HubEventProcessor подписан на топик {}", hubEventsTopic);

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, HubEventAvro> records = hubEventConsumer.poll(Duration.ofMillis(1000));
                for (var record : records) {
                    handleHubEvent(record.value());
                }
                if (!records.isEmpty()) {
                    hubEventConsumer.commitSync();
                }
            }
        } catch (WakeupException e) {
            log.info("HubEventProcessor получил сигнал остановки (wakeup).");
        } catch (Exception e) {
            log.error("Ошибка в HubEventProcessor", e);
        } finally {
            try {
                hubEventConsumer.close();
            } catch (Exception e) {
                log.error("Ошибка при закрытии hubEventConsumer", e);
            }
            log.info("HubEventProcessor завершил работу, consumer закрыт.");
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Инициирован graceful shutdown для HubEventProcessor...");
        hubEventConsumer.wakeup();
    }

    private void handleHubEvent(HubEventAvro event) {
        String payloadClassName = event.getPayload().getClass().getSimpleName();
        HubEventHandler handler = handlerFactory.getHubMap().get(payloadClassName);
        if (handler != null) {
            handler.handle(event);
        } else {
            log.warn("Не найден обработчик для события типа: {}", payloadClassName);
        }
    }
}