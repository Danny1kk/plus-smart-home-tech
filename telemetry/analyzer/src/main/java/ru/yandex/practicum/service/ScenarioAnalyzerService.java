package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioAnalyzerService {

    private final ScenarioRepository scenarioRepository;

    public void analyze(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> sensorStates = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        for (Scenario scenario : scenarios) {
            boolean allConditionsMatch = true;

            for (ScenarioCondition sc : scenario.getConditions()) {
                SensorStateAvro actualState = sensorStates.get(sc.getSensor().getId());

                if (actualState == null || !matchCondition(sc, actualState)) {
                    allConditionsMatch = false;
                    break;
                }
            }

            if (allConditionsMatch && !scenario.getActions().isEmpty()) {
                log.info("Все условия сценария '{}' выполнены. Запуск действий...", scenario.getName());
            }
        }
    }

    private boolean matchCondition(ScenarioCondition sc, SensorStateAvro state) {
        Object rawValue = state.getData();
        if (!(rawValue instanceof Number)) {
            return false;
        }
        int actualValue = ((Number) rawValue).intValue();
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