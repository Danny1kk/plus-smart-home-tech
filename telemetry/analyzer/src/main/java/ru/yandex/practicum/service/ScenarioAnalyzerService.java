package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.HubRouterClient;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
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
            if (scenario.getConditions() == null || scenario.getConditions().isEmpty()) continue;

            boolean allConditionsMatch = true;
            for (ScenarioCondition sc : scenario.getConditions()) {
                SensorStateAvro actualState = sensorStates.get(sc.getId().getSensorId());

                if (actualState == null || !matchCondition(sc, actualState)) {
                    allConditionsMatch = false;
                    break;
                }
            }

            if (allConditionsMatch) {
                log.info("Сценарий '{}' сработал! Отправка команд...", scenario.getName());
                scenario.getActions().forEach(action ->
                        routerClient.sendAction(
                                hubId,
                                scenario.getName(),
                                action.getId().getSensorId(),
                                action.getAction(),
                                Instant.ofEpochMilli(snapshot.getTimestamp().toEpochMilli())
                        )
                );
            }
        }
    }

    private boolean matchCondition(ScenarioCondition sc, SensorStateAvro state) {
        Object data = state.getData();
        Integer actualValue = null;

        if (data instanceof org.apache.avro.specific.SpecificRecordBase) {
            org.apache.avro.specific.SpecificRecordBase record = (org.apache.avro.specific.SpecificRecordBase) data;

            if (record.getSchema().getField("temperature") != null) actualValue = ((Number) record.get("temperature")).intValue();
            else if (record.getSchema().getField("temperatureC") != null) actualValue = ((Number) record.get("temperatureC")).intValue();
            else if (record.getSchema().getField("luminosity") != null) actualValue = ((Number) record.get("luminosity")).intValue();
            else if (record.getSchema().getField("state") != null) actualValue = (Boolean) record.get("state") ? 1 : 0;
            else if (record.getSchema().getField("isOpen") != null) actualValue = (Boolean) record.get("isOpen") ? 1 : 0;
            else if (record.getSchema().getField("motion") != null) actualValue = (Boolean) record.get("motion") ? 1 : 0;
            else if (record.getSchema().getField("motionDetected") != null) actualValue = (Boolean) record.get("motionDetected") ? 1 : 0;
        }

        if (actualValue == null) return false;

        int target = sc.getCondition().getValue();
        return switch (sc.getCondition().getOperation()) {
            case EQUALS -> actualValue == target;
            case GREATER_THAN -> actualValue > target;
            case LOWER_THAN -> actualValue < target;
        };
    }
}