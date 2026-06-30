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

    private boolean handleOperation(Condition condition, String currentValue, String typeName) {
        if (condition.getOperation() == null || currentValue == null) {
            return false;
        }

        String targetValue = condition.getValue() != null ? condition.getValue().toString() : "null";
        String opName = condition.getOperation().name();

//        if ("SWITCH".equals(typeName) || "MOTION".equals(typeName)) {
//            if (!"EQUALS".equals(opName)) {
//                return false;
//            }
//            boolean targetBool = "true".equalsIgnoreCase(targetValue) || "1".equals(targetValue);
//            boolean currentBool = "true".equalsIgnoreCase(currentValue) || "1".equals(currentValue);
//
//            return targetBool == currentBool;
//        }

        if ("true".equalsIgnoreCase(currentValue)) currentValue = "1";
        if ("false".equalsIgnoreCase(currentValue)) currentValue = "0";
        if ("true".equalsIgnoreCase(targetValue)) targetValue = "1";
        if ("false".equalsIgnoreCase(targetValue)) targetValue = "0";

        try {
            double current = Double.parseDouble(currentValue);
            double target = Double.parseDouble(targetValue);

            return switch (opName) {
                case "EQUALS" -> Math.abs(current - target) < 0.0001;
                case "GREATER_THAN" -> current > target;
                case "LESS_THAN", "LOWER_THAN" -> current < target;
                default -> false;
            };
        } catch (NumberFormatException e) {
            log.warn("Не удалось распарсить числа для датчика {}: текущее={}, эталон={}",
                    typeName, currentValue, targetValue);
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
                    MotionSensorAvro motion = (MotionSensorAvro) data;
                    yield handleOperation(condition, String.valueOf(motion.getMotion()), typeName);
                }
                case "LUMINOSITY" -> {
                    LightSensorAvro light = (LightSensorAvro) data;
                    yield handleOperation(condition, String.valueOf(light.getLuminosity()), typeName);
                }
                case "SWITCH" -> {
                    SwitchSensorAvro sw = (SwitchSensorAvro) data;
                    yield handleOperation(condition, String.valueOf(sw.getState()), typeName);
                }
                case "TEMPERATURE" -> {
                    ClimateSensorAvro climate = (ClimateSensorAvro) data;
                    yield handleOperation(condition, String.valueOf(climate.getTemperatureC()), typeName);
                }
                case "CO2LEVEL" -> {
                    ClimateSensorAvro climate = (ClimateSensorAvro) data;
                    yield handleOperation(condition, String.valueOf(climate.getCo2Level()), typeName);
                }
                case "HUMIDITY" -> {
                    ClimateSensorAvro climate = (ClimateSensorAvro) data;
                    yield handleOperation(condition, String.valueOf(climate.getHumidity()), typeName);
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