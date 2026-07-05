package ru.yandex.practicum.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.*;

import java.util.ArrayList;
import java.util.List;

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

        Scenario scenario = scenarioRepository.findByHubIdAndName(hub.getHubId(), avro.getName())
                .orElseGet(() -> Scenario.builder()
                        .hubId(hub.getHubId())
                        .name(avro.getName())
                        .build());

        if (scenario.getId() != null) {
            scenarioConditionRepository.deleteByScenarioId(scenario.getId());
            scenarioActionRepository.deleteByScenarioId(scenario.getId());

            scenario.getConditions().clear();
            scenario.getActions().clear();
            scenarioRepository.flush();
        } else {
            scenario = scenarioRepository.save(scenario);
        }

        processConditions(scenario, avro, hub.getHubId());
        processActions(scenario, avro, hub.getHubId());

        scenarioRepository.save(scenario);
        scenarioRepository.flush();

        log.info("DEBUG: Сценарий {} успешно обновлен/сохранен со всеми связями", scenario.getName());
    }

    private void processConditions(Scenario scenario, ScenarioAddedEventAvro avro, String hubId) {
        if (avro.getConditions() == null) return;

        avro.getConditions().forEach(cDto -> {
            Sensor sensor = sensorRepository.findById(cDto.getSensorId())
                    .orElseGet(() -> sensorRepository.save(Sensor.builder()
                            .id(cDto.getSensorId())
                            .hubId(hubId)
                            .sensorType(cDto.getType().name())
                            .build()));

            ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                    .scenario(scenario)
                    .sensorId(cDto.getSensorId())
                    .type(cDto.getType().name())
                    .operation(cDto.getOperation().name())
                    .value(String.valueOf(cDto.getValue()))
                    .build();

            scenario.addCondition(scenarioCondition);
        });
    }

    private void processActions(Scenario scenario, ScenarioAddedEventAvro avro, String hubId) {
        if (avro.getActions() == null) return;

        List<Action> actionsToSave = new ArrayList<>();

        avro.getActions().forEach(aDto -> {
            Sensor sensor = sensorRepository.findById(aDto.getSensorId())
                    .orElseGet(() -> sensorRepository.save(Sensor.builder()
                            .id(aDto.getSensorId())
                            .hubId(hubId)
                            .sensorType(aDto.getType() != null ? aDto.getType().name() : null)
                            .build()));

            Action action = Action.builder()
                    .type(ActionTypeAvro.valueOf(aDto.getType().name()))
                    .value(asInteger(aDto.getValue()))
                    .build();

            actionsToSave.add(action);

            ScenarioAction scenarioAction = ScenarioAction.builder()
                    .scenario(scenario)
                    .sensor(sensor)
                    .action(action)
                    .id(new ScenarioActionId(scenario.getId(), sensor.getId()))
                    .build();

            scenario.addAction(scenarioAction);
        });

        actionRepository.saveAll(actionsToSave);
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        if (value instanceof String || value instanceof CharSequence) {
            String str = value.toString().trim();
            if ("true".equalsIgnoreCase(str)) return 1;
            if ("false".equalsIgnoreCase(str)) return 0;
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                log.warn("Не удалось распарсить строку в Integer: {}", value);
                return 0;
            }
        }
        return 0;
    }
}