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

    private boolean handleOperation(Condition condition, String currentValue) {
        if (condition.getOperation() == null || currentValue == null) {
            return false;
        }

        String targetValue = condition.getValue() != null ? condition.getValue().toString() : "null";
        String opName = condition.getOperation().name();

        boolean targetBool = targetValue.equals("true") || targetValue.equals("1");
        boolean currentBool = currentValue.equals("true") || currentValue.equals("1");

        if ("EQUALS".equals(opName)) {
            return targetBool == currentBool;
        }

        try {
            int current = Integer.parseInt(currentValue);
            int target = Integer.parseInt(targetValue);

            return switch (opName) {
                case "GREATER_THAN" -> current > target;
                case "LESS_THAN" -> current < target;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean checkCondition(Condition condition, String sensorId,
                                   Map<String, SensorStateAvro> sensorStateMap) {

        SensorStateAvro sensorState = sensorStateMap.get(sensorId);
        if (sensorState == null || condition.getType() == null) {
            return false;
        }

        String typeName = condition.getType().name();
        Object data = sensorState.getData();
        if (data == null) return false;

        boolean result = false;

        try {
            result = switch (typeName) {
                case "MOTION" -> {
                    if (data instanceof MotionSensorAvro motion) {
                        yield handleOperation(condition, String.valueOf(motion.getMotion()));
                    }
                    yield false;
                }
                case "LUMINOSITY" -> {
                    if (data instanceof LightSensorAvro light) {
                        yield handleOperation(condition, String.valueOf(light.getLuminosity()));
                    }
                    yield false;
                }
                case "SWITCH" -> {
                    if (data instanceof SwitchSensorAvro sw) {
                        yield handleOperation(condition, String.valueOf(sw.getState()));
                    }
                    yield false;
                }
                case "TEMPERATURE" -> {
                    if (data instanceof ClimateSensorAvro climate) {
                        yield handleOperation(condition, String.valueOf(climate.getTemperatureC()));
                    }
                    yield false;
                }
                case "CO2LEVEL" -> {
                    if (data instanceof ClimateSensorAvro climate) {
                        yield handleOperation(condition, String.valueOf(climate.getCo2Level()));
                    }
                    yield false;
                }
                case "HUMIDITY" -> {
                    if (data instanceof ClimateSensorAvro climate) {
                        yield handleOperation(condition, String.valueOf(climate.getHumidity()));
                    }
                    yield false;
                }
                default -> false;
            };
        } catch (Exception e) {
            log.error("Ошибка при разборе данных датчика {}: ", sensorId, e);
            return false;
        }

        log.info("Проверяю условие датчика {}: тип={}, операция={}, эталон={}, текущее={}, результат: {}",
                sensorId, typeName, condition.getOperation(), condition.getValue(), sensorState.getData(), result);

        return result;
    }
}