package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final ScenarioRepository scenarioRepository;

    @Override
    public void run() {
        Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

        try {
            hubEventConsumer.subscribe(Collections.singletonList("telemetry.hubs.v1"));
            log.info("HubEventProcessor запущен, подписка на топик telemetry.hubs.v1 оформлена");

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = hubEventConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    handleHubEvent(record.value());
                    manageOffsets(record, currentOffsets, hubEventConsumer);
                }
            }
        } catch (WakeupException e) {
            log.info("HubEventProcessor получил сигнал остановки (wakeup).");
        } finally {
            hubEventConsumer.close();
        }
    }

    private void handleHubEvent(HubEventAvro event) {
        String hubId = event.getHubId();
        Object payload = event.getPayload();

        if (payload instanceof ScenarioAddedEventAvro) {
            ScenarioAddedEventAvro addedEvent = (ScenarioAddedEventAvro) payload;

            Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, addedEvent.getName())
                    .orElse(new Scenario());

            scenario.setHubId(hubId);
            scenario.setName(addedEvent.getName());

            var conditions = addedEvent.getConditions().stream().map(avro -> {
                Condition baseCondition = new Condition();
                baseCondition.setType(avro.getType());
                baseCondition.setOperation(avro.getOperation());

                if (avro.getValue() != null) {
                    if (avro.getValue() instanceof Number) {
                        baseCondition.setValue(((Number) avro.getValue()).intValue());
                    } else {
                        baseCondition.setValue(Integer.parseInt(avro.getValue().toString()));
                    }
                }

                Sensor sensor = Sensor.builder().id(avro.getSensorId()).hubId(hubId).build();

                ScenarioCondition scenarioCondition = new ScenarioCondition();
                scenarioCondition.setScenario(scenario);
                scenarioCondition.setCondition(baseCondition);
                scenarioCondition.setSensor(sensor);
                scenarioCondition.setId(new ScenarioConditionId());

                return scenarioCondition;
            }).collect(Collectors.toList());

            if (scenario.getConditions() == null) {
                scenario.setConditions(conditions);
            } else {
                scenario.getConditions().clear();
                scenario.getConditions().addAll(conditions);
            }

            var actions = addedEvent.getActions().stream().map(avro -> {
                Action baseAction = new Action();
                baseAction.setType(avro.getType());

                if (avro.getValue() != null) {
                    if (avro.getValue() instanceof Number) {
                        baseAction.setValue(((Number) avro.getValue()).intValue());
                    } else {
                        baseAction.setValue(Integer.parseInt(avro.getValue().toString()));
                    }
                }

                Sensor sensor = Sensor.builder().id(avro.getSensorId()).hubId(hubId).build();

                ScenarioAction scenarioAction = new ScenarioAction();
                scenarioAction.setScenario(scenario);
                scenarioAction.setAction(baseAction);
                scenarioAction.setSensor(sensor);
                scenarioAction.setId(new ScenarioActionId());

                return scenarioAction;
            }).collect(Collectors.toSet());

            if (scenario.getActions() == null) {
                scenario.setActions(actions);
            } else {
                scenario.getActions().clear();
                scenario.getActions().addAll(actions);
            }

            scenarioRepository.save(scenario);
            log.info("Сценарий успешно сохранен или обновлен: '{}' для хаба {}", scenario.getName(), hubId);

        } else if (payload instanceof ScenarioRemovedEventAvro) {
            ScenarioRemovedEventAvro removedEvent = (ScenarioRemovedEventAvro) payload;
            scenarioRepository.findByHubIdAndName(hubId, removedEvent.getName())
                    .ifPresent(scenarioRepository::delete);
            log.info("Сценарий успешно удален: '{}' для хаба {}", removedEvent.getName(), hubId);
        }
    }

    private void manageOffsets(ConsumerRecord<String, HubEventAvro> record, Map<TopicPartition, OffsetAndMetadata> currentOffsets, KafkaConsumer<String, HubEventAvro> consumer) {
        TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
        currentOffsets.put(topicPartition, new OffsetAndMetadata(record.offset() + 1));
        consumer.commitAsync(currentOffsets, (offsets, exception) -> {
            if (exception != null) {
                log.error("Ошибка асинхронной фиксации смещений для {}", offsets, exception);
            }
        });
    }
}