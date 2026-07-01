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
        if (hubId == null || hubId.isBlank()) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА: Из Кафки пришел снапшот с пустым hubId!");
            return;
        }

        List<Scenario> scenariosList = scenarioRepository.findByHubId(hubId);

        if (scenariosList == null || scenariosList.isEmpty()) {
            return;
        }

        Map<String, SensorStateAvro> sensorStateMap = sensorsSnapshotAvro.getSensorsState();

        scenariosList.forEach(scenario -> {
            if (handleScenario(scenario, sensorStateMap)) {
                log.info("DEBUG_LOG: !!! УСЛОВИЯ СЦЕНАРИЯ '{}' ВЫПОЛНЕНЫ !!!", scenario.getName());
                sendScenarioAction(scenario, sensorsSnapshotAvro);
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
            return true;
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
        String opName = condition.getOperation().name().toUpperCase();

        if (opName.equals("EQUALS")) {
            if (currentValue.equalsIgnoreCase(targetValue)) return true;

            boolean currentBool = "true".equalsIgnoreCase(currentValue) || "on".equalsIgnoreCase(currentValue) || "1".equals(currentValue);
            boolean targetBool = "true".equalsIgnoreCase(targetValue) || "on".equalsIgnoreCase(targetValue) || "1".equals(targetValue);
            if (currentBool == targetBool && ("true".equalsIgnoreCase(currentValue) || "false".equalsIgnoreCase(currentValue) || "on".equalsIgnoreCase(currentValue) || "off".equalsIgnoreCase(currentValue))) {
                return true;
            }
        }

        if ("true".equalsIgnoreCase(currentValue) || "on".equalsIgnoreCase(currentValue)) currentValue = "1";
        if ("false".equalsIgnoreCase(currentValue) || "off".equalsIgnoreCase(currentValue)) currentValue = "0";
        if ("true".equalsIgnoreCase(targetValue) || "on".equalsIgnoreCase(targetValue)) targetValue = "1";
        if ("false".equalsIgnoreCase(targetValue) || "off".equalsIgnoreCase(targetValue)) targetValue = "0";

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

    private boolean checkCondition(Condition condition, String sensorId, Map<String, SensorStateAvro> sensorStateMap) {
        if (sensorStateMap == null || !sensorStateMap.containsKey(sensorId)) {
            return false;
        }

        SensorStateAvro sensorState = sensorStateMap.get(sensorId);
        if (sensorState == null || condition.getType() == null) {
            return false;
        }

        String typeName = condition.getType().name();
        String currentValue = getSensorValue(sensorState, typeName);

        if (currentValue == null) {
            return false;
        }

        boolean result = handleOperation(condition, currentValue, typeName);

        log.info("Проверяю условие датчика {}: тип={}, операция={}, эталон={}, текущее={}, результат: {}",
                sensorId, typeName, condition.getOperation(), condition.getValue(), currentValue, result);

        return result;
    }

    private String getSensorValue(SensorStateAvro state, String conditionType) {
        if (state == null || state.getData() == null) return null;

        Object data = state.getData();

        return switch (conditionType.toUpperCase()) {
            case "MOTION" -> {
                if (data instanceof MotionSensorAvro motion) {
                    yield String.valueOf(motion.getMotion());
                }
                yield null;
            }
            case "LUMINOSITY" -> {
                if (data instanceof LightSensorAvro light) {
                    yield String.valueOf(light.getLuminosity());
                }
                yield null;
            }
            case "SWITCH" -> {
                if (data instanceof SwitchSensorAvro sw) {
                    yield String.valueOf(sw.getState());
                }
                yield null;
            }
            case "TEMPERATURE" -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield String.valueOf(climate.getTemperatureC());
                }
                yield null;
            }
            case "CO2LEVEL" -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield String.valueOf(climate.getCo2Level());
                }
                yield null;
            }
            case "HUMIDITY" -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield String.valueOf(climate.getHumidity());
                }
                yield null;
            }
            default -> null;
        };
    }
}