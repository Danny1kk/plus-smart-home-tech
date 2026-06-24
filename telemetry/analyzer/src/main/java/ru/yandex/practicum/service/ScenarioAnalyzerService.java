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

        log.info("--- ANALYZE [DEBUG]: Хаб {} прислал датчики: {} ---", hubId, sensorStates.keySet());

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        log.info("--- ANALYZE [DEBUG]: В базе найдено {} сценариев для хаба {} ---", scenarios.size(), hubId);

        for (Scenario scenario : scenarios) {
            log.info("--- ANALYZE [DEBUG]: Проверка сценария '{}', условий: {} ---", scenario.getName(), scenario.getConditions().size());

            boolean allConditionsMatch = true;

            for (ScenarioCondition sc : scenario.getConditions()) {
                String dbId = sc.getId().getSensorId();
                SensorStateAvro state = sensorStates.get(dbId);

                log.info("--- ANALYZE [DEBUG]: Датчик сценария ID='{}' | Найден в снапшоте? {} ---", dbId, (state != null));

                if (state == null || !matchCondition(sc, state)) {
                    allConditionsMatch = false;
                    break;
                }
            }

            if (allConditionsMatch && !scenario.getConditions().isEmpty()) {
                log.info("[DEBUG] Сценарий {} ВЫПОЛНЕН! Отправляем сигнал", scenario.getName());
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
        if (!(data instanceof org.apache.avro.specific.SpecificRecordBase record)) {
            log.warn("Неизвестный формат данных для датчика {}", sc.getId().getSensorId());
            return false;
        }

        Object value = null;
        String[] possibleFields = {"temperature", "temperatureC", "luminosity", "state", "isOpen", "motion", "motionDetected"};

        for (String field : possibleFields) {
            try {
                value = record.get(field);
                if (value != null) break;
            } catch (Exception ignored) {}
        }

        if (value == null) {
            log.warn("Значение не найдено для датчика {}. Доступные поля: {}", sc.getId().getSensorId(), record.getSchema().getFields());
            return false;
        }

        int actualValue = (value instanceof Boolean b) ? (b ? 1 : 0) : ((Number) value).intValue();
        int target = sc.getCondition().getValue();
        boolean result = switch (sc.getCondition().getOperation()) {
            case EQUALS -> actualValue == target;
            case GREATER_THAN -> actualValue > target;
            case LOWER_THAN -> actualValue < target;
        };

        log.info("DEBUG: Датчик {} | Получено: {} | Цель: {} | Результат: {}", sc.getId().getSensorId(), actualValue, target, result);
        return result;
    }
}