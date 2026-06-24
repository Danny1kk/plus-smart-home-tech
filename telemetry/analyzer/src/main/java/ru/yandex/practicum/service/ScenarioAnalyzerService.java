package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.HubRouterClient;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioAnalyzerService {

    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient routerClient;

    public void analyze(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> sensorStates = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        for (Scenario scenario : scenarios) {
            if (scenario.getConditions() == null || scenario.getConditions().isEmpty()) {
                continue;
            }

            boolean allConditionsMatch = true;

            for (ScenarioCondition sc : scenario.getConditions()) {
                SensorStateAvro actualState = sensorStates.get(sc.getId().getSensorId());

                if (actualState == null || !matchCondition(sc, actualState)) {
                    allConditionsMatch = false;
                    break;
                }
            }

            if (allConditionsMatch && !scenario.getActions().isEmpty()) {
                log.info("Все условия сценария '{}' выполнены. Запуск действий...", scenario.getName());
                scenario.getActions().forEach(scenarioAction ->
                        routerClient.sendAction(
                                snapshot.getHubId(),
                                scenario.getName(),
                                scenarioAction.getId().getSensorId(),
                                scenarioAction.getAction(),
                                Instant.ofEpochMilli(snapshot.getTimestamp().toEpochMilli())
                        )
                );
            }
        }
    }

    private boolean matchCondition(ScenarioCondition sc, SensorStateAvro state) {
        Object data = state.getData();
        int actualValue;

        if (data instanceof org.apache.avro.specific.SpecificRecordBase) {
            org.apache.avro.specific.SpecificRecordBase avroRecord = (org.apache.avro.specific.SpecificRecordBase) data;

            String className = data.getClass().getSimpleName();
            switch (className) {
                case "ClimateSensorAvro":
                    if (avroRecord.get("temperature") != null) {
                        actualValue = ((Number) avroRecord.get("temperature")).intValue();
                    } else if (avroRecord.get("temperatureC") != null) {
                        actualValue = ((Number) avroRecord.get("temperatureC")).intValue();
                    } else {
                        actualValue = ((Number) avroRecord.get("temperature_c")).intValue();
                    }
                    break;
                case "LightSensorAvro":
                    actualValue = ((Number) avroRecord.get("luminosity")).intValue();
                    break;
                case "SwitchSensorAvro":
                    Object switchState = avroRecord.get("state") != null ? avroRecord.get("state") : avroRecord.get("isOpen");
                    actualValue = (Boolean) switchState ? 1 : 0;
                    break;
                case "MotionSensorAvro":
                    Object motionState = avroRecord.get("motion") != null ? avroRecord.get("motion") : avroRecord.get("motionDetected");
                    actualValue = (Boolean) motionState ? 1 : 0;
                    break;
                default:
                    log.warn("Неизвестный тип Avro-данных датчика: {}", className);
                    return false;
            }
        } else {
            log.warn("Данные датчика не являются Avro-записью: {}", data != null ? data.getClass().getName() : "null");
            return false;
        }

        int targetValue = sc.getCondition().getValue();
        ConditionOperationAvro op = sc.getCondition().getOperation();

        if (op == ConditionOperationAvro.EQUALS) {
            return actualValue == targetValue;
        } else if (op == ConditionOperationAvro.GREATER_THAN) {
            return actualValue > targetValue;
        } else if (op == ConditionOperationAvro.LOWER_THAN) {
            return actualValue < targetValue;
        }
        return false;
    }
}