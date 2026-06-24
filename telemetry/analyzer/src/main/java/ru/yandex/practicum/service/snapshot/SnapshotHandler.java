package ru.yandex.practicum.service.snapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.HubRouterClient;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotHandler {

    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient routerClient;

    @Transactional(readOnly = true)
    public void handle(SensorsSnapshotAvro sensorsSnapshotAvro) {
        String hubId = sensorsSnapshotAvro.getHubId();

        List<Scenario> scenariosList = scenarioRepository.findByHubId(hubId);

        log.info("DEBUG_LOG: Анализатор получил снапшот для хаба '{}'. Найдено сценариев в БД: {}",
                hubId, (scenariosList != null ? scenariosList.size() : "NULL"));

        if (scenariosList == null || scenariosList.isEmpty()) {
            log.info("DEBUG_LOG: Сценарии не найдены, прекращаю анализ для хаба '{}'", hubId);
            return;
        }

        Map<String, SensorStateAvro> sensorStateMap = sensorsSnapshotAvro.getSensorsState();

        scenariosList.forEach(scenario -> {
            log.info("DEBUG_LOG: Проверяю сценарий '{}'. Условий в БД: {}",
                    scenario.getName(),
                    (scenario.getConditions() != null ? scenario.getConditions().size() : "NULL"));

            if (handleScenario(scenario, sensorStateMap)) {
                log.info("DEBUG_LOG: !!! УСЛОВИЯ СЦЕНАРИЯ '{}' ВЫПОЛНЕНЫ !!!", scenario.getName());
                sendScenarioAction(scenario, sensorsSnapshotAvro);
            } else {
                log.info("DEBUG_LOG: Условия сценария '{}' НЕ выполнены", scenario.getName());
            }
        });
    }

    private void sendScenarioAction(Scenario scenario, SensorsSnapshotAvro snapshot) {
        scenario.getActions().forEach(scenarioAction -> {
            log.info("gRPC: Отправка команды на устройство {} в рамках сценария {}",
                    scenarioAction.getId().getSensorId(), scenario.getName());
            routerClient.sendAction(
                    snapshot.getHubId(),
                    scenario.getName(),
                    scenarioAction.getId().getSensorId(),
                    scenarioAction.getAction(),
                    Instant.ofEpochMilli(snapshot.getTimestamp().toEpochMilli())
            );
        });
    }

    private boolean handleScenario(Scenario scenario, Map<String, SensorStateAvro> sensorStateMap) {
        List<ScenarioCondition> scenarioConditions = scenario.getConditions();

        if (scenarioConditions == null || scenarioConditions.isEmpty()) {
            return false;
        }

        return scenarioConditions.stream()
                .allMatch(sc -> checkCondition(sc.getCondition(),
                        sc.getSensor().getId(),
                        sensorStateMap));
    }

    private boolean handleOperation(Condition condition, Integer currentValue) {
        Integer targetValue = condition.getValue();
        if (condition.getOperation() == null || currentValue == null) {
            return false;
        }

        String opName = condition.getOperation().name();
        return switch (opName) {
            case "EQUALS" -> targetValue.equals(currentValue);
            case "GREATER_THAN" -> currentValue > targetValue;
            case "LOWER_THAN" -> currentValue < targetValue;
            default -> {
                log.warn("Неизвестная операция: {}", opName);
                yield false;
            }
        };
    }

    private boolean checkCondition(Condition condition, String sensorId,
                                   Map<String, SensorStateAvro> sensorStateMap) {

        SensorStateAvro sensorState = sensorStateMap.get(sensorId);
        if (sensorState == null) {
            return false;
        }

        if (condition.getType() == null) {
            return false;
        }

        String typeName = condition.getType().name();
        boolean result = switch (typeName) {
            case "MOTION" -> {
                MotionSensorAvro motion = (MotionSensorAvro) sensorState.getData();
                yield handleOperation(condition, motion.getMotion() ? 1 : 0);
            }
            case "LUMINOSITY" -> {
                LightSensorAvro light = (LightSensorAvro) sensorState.getData();
                yield handleOperation(condition, light.getLuminosity());
            }
            case "SWITCH" -> {
                SwitchSensorAvro sw = (SwitchSensorAvro) sensorState.getData();
                yield handleOperation(condition, sw.getState() ? 1 : 0);
            }
            case "TEMPERATURE" -> {
                ClimateSensorAvro climate = (ClimateSensorAvro) sensorState.getData();
                yield handleOperation(condition, climate.getTemperatureC());
            }
            case "CO2LEVEL" -> {
                ClimateSensorAvro climate = (ClimateSensorAvro) sensorState.getData();
                yield handleOperation(condition, climate.getCo2Level());
            }
            case "HUMIDITY" -> {
                ClimateSensorAvro climate = (ClimateSensorAvro) sensorState.getData();
                yield handleOperation(condition, climate.getHumidity());
            }
            default -> false;
        };

        log.info("Проверяю условие датчика {}: тип={}, операция={}, эталон={}, текущее={}. Результат: {}",
                sensorId, typeName, condition.getOperation(), condition.getValue(), sensorState.getData(), result);

        return result;
    }
}