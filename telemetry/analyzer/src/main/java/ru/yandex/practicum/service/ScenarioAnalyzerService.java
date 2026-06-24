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
        if (!(data instanceof org.apache.avro.specific.SpecificRecordBase)) {
            log.warn("Данные не являются Avro записью для датчика {}", sc.getId().getSensorId());
            return false;
        }

        org.apache.avro.specific.SpecificRecordBase record = (org.apache.avro.specific.SpecificRecordBase) data;
        Integer actualValue = null;

        log.debug("Анализ датчика {}: запись {}", sc.getId().getSensorId(), record);

        for (org.apache.avro.Schema.Field field : record.getSchema().getFields()) {
            Object val = record.get(field.name());
            if (val instanceof Number) {
                actualValue = ((Number) val).intValue();
                break;
            } else if (val instanceof Boolean) {
                actualValue = (Boolean) val ? 1 : 0;
                break;
            }
        }

        if (actualValue == null) {
            log.warn("Не удалось извлечь значение из датчика {}", sc.getId().getSensorId());
            return false;
        }

        int target = sc.getCondition().getValue();
        boolean result = switch (sc.getCondition().getOperation()) {
            case EQUALS -> actualValue == target;
            case GREATER_THAN -> actualValue > target;
            case LOWER_THAN -> actualValue < target;
        };

        log.info("Проверка условия: датчик={}, значение={}, цель={}, результат={}",
                sc.getId().getSensorId(), actualValue, target, result);
        return result;
    }
}