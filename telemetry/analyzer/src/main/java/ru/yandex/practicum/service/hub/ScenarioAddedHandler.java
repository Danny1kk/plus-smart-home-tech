package ru.yandex.practicum.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedHandler implements HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final SensorRepository sensorRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;

    @Override
    public String getPayloadType() {
        return ScenarioAddedEventAvro.class.getSimpleName();
    }

    @Transactional
    @Override
    public void handle(HubEventAvro hub) {
        ScenarioAddedEventAvro avro = (ScenarioAddedEventAvro) hub.getPayload();
        log.info("DEBUG: Пытаюсь сохранить сценарий {} для хаба {}", avro.getName(), hub.getHubId());

        scenarioRepository.findByHubIdAndName(hub.getHubId(), avro.getName())
                .ifPresent(s -> {
                    scenarioRepository.delete(s);
                    scenarioRepository.flush();
                });

        Scenario scenario = scenarioRepository.save(Scenario.builder()
                .hubId(hub.getHubId())
                .name(avro.getName())
                .build());

        processConditions(scenario, avro, hub.getHubId());
        processActions(scenario, avro, hub.getHubId());

        scenarioRepository.save(scenario);
        log.info("DEBUG: Сценарий сохранен в базу");
        log.info("Сценарий '{}' для хаба {} успешно сохранен.", avro.getName(), hub.getHubId());
    }

    private void processConditions(Scenario scenario, ScenarioAddedEventAvro avro, String hubId) {
        avro.getConditions().forEach(cDto -> {
            Sensor sensor = sensorRepository.findById(cDto.getSensorId())
                    .orElseGet(() -> sensorRepository.save(Sensor.builder()
                            .id(cDto.getSensorId())
                            .hubId(hubId)
                            .build()));

            Condition condition = conditionRepository.save(Condition.builder()
                    .type(cDto.getType())
                    .operation(cDto.getOperation())
                    .value(asInteger(cDto.getValue()))
                    .build());

            ScenarioCondition scenarioCondition = scenarioConditionRepository.save(
                    ScenarioCondition.builder()
                            .scenario(scenario)
                            .sensor(sensor)
                            .condition(condition)
                            .id(new ScenarioConditionId(scenario.getId(), sensor.getId(), condition.getId()))
                            .build());

            scenario.getConditions().add(scenarioCondition);
        });
    }

    private void processActions(Scenario scenario, ScenarioAddedEventAvro avro, String hubId) {
        avro.getActions().forEach(aDto -> {
            Sensor sensor = sensorRepository.findById(aDto.getSensorId())
                    .orElseGet(() -> sensorRepository.save(Sensor.builder()
                            .id(aDto.getSensorId())
                            .hubId(hubId)
                            .build()));

            Action action = actionRepository.save(Action.builder()
                    .type(aDto.getType())
                    .value(aDto.getValue())
                    .build());

            ScenarioAction scenarioAction = scenarioActionRepository.save(
                    ScenarioAction.builder()
                            .scenario(scenario)
                            .sensor(sensor)
                            .action(action)
                            .id(new ScenarioActionId(scenario.getId(), sensor.getId(), action.getId()))
                            .build());

            scenario.getActions().add(scenarioAction);
        });
    }

    private Integer asInteger(Object value) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Boolean) return (Boolean) value ? 1 : 0;
        return 0;
    }
}