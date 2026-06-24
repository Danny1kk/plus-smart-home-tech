package ru.yandex.practicum.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.model.ScenarioActionId;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.model.ScenarioConditionId;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioActionRepository;
import ru.yandex.practicum.repository.ScenarioConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.HashSet;

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
        ScenarioAddedEventAvro avro = getScenarioAddedAvro(hub);

        scenarioRepository.findByHubIdAndName(hub.getHubId(), avro.getName())
                .ifPresent(existingScenario -> {
                    scenarioRepository.delete(existingScenario);
                    scenarioRepository.flush();
                });

        Scenario scenario = scenarioRepository.save(
                Scenario.builder()
                        .hubId(hub.getHubId())
                        .name(avro.getName())
                        .conditions(new HashSet<>())
                        .actions(new HashSet<>())
                        .build()
        );

        processConditions(scenario, hub);
        processActions(scenario, hub);
    }

    private ScenarioAddedEventAvro getScenarioAddedAvro(HubEventAvro hub) {
        return (ScenarioAddedEventAvro) hub.getPayload();
    }

    private void processActions(Scenario scenario, HubEventAvro hub) {
        ScenarioAddedEventAvro avro = getScenarioAddedAvro(hub);
        avro.getActions().forEach(aDto -> {
            Sensor sensor = sensorRepository.findById(aDto.getSensorId())
                    .orElseGet(() -> sensorRepository.save(
                            Sensor.builder()
                                    .id(aDto.getSensorId())
                                    .hubId(hub.getHubId())
                                    .build()));
            Action action = actionRepository.save(
                    Action.builder()
                            .type(aDto.getType())
                            .value(aDto.getValue())
                            .build());

            ScenarioAction scenarioAction = scenarioActionRepository.save(
                    ScenarioAction.builder()
                            .scenario(scenario)
                            .sensor(sensor)
                            .action(action)
                            .id(new ScenarioActionId(
                                    scenario.getId(),
                                    sensor.getId(),
                                    action.getId()))
                            .build());

            scenario.getActions().add(scenarioAction);
        });
    }

    private void processConditions(Scenario scenario, HubEventAvro hub) {
        ScenarioAddedEventAvro avro = getScenarioAddedAvro(hub);
        avro.getConditions().forEach(cDto -> {
            Sensor sensor = sensorRepository.findById(cDto.getSensorId())
                    .orElseGet(() -> sensorRepository.save(
                            Sensor.builder()
                                    .id(cDto.getSensorId())
                                    .hubId(hub.getHubId())
                                    .build()));
            Condition condition = conditionRepository.save(
                    Condition.builder()
                            .type(cDto.getType())
                            .operation(cDto.getOperation())
                            .value(asInteger(cDto.getValue()))
                            .build());

            ScenarioCondition scenarioCondition = scenarioConditionRepository.save(
                    ScenarioCondition.builder()
                            .scenario(scenario)
                            .sensor(sensor)
                            .condition(condition)
                            .id(new ScenarioConditionId(
                                    scenario.getId(),
                                    sensor.getId(),
                                    condition.getId()))
                            .build());

            scenario.getConditions().add(scenarioCondition);
        });
    }

    private Integer asInteger(Object value) {
        return value instanceof Integer
                ? (Integer) value
                : ((Boolean) value ? 1 : 0);
    }
}