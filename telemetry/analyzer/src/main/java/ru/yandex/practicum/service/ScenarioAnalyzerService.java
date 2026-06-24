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

        log.info("--- [DEBUG] ANALYZE: Хаб {} прислал датчики: {} ---", hubId, sensorStates.keySet());

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        log.info("--- [DEBUG] ANALYZE: В базе найдено {} сценариев для хаба {} ---", scenarios.size(), hubId);

        for (Scenario scenario : scenarios) {
            boolean allConditionsMatch = true;

            for (ScenarioCondition sc : scenario.getConditions()) {
                String dbId = sc.getId().getSensorId();
                SensorStateAvro state = sensorStates.get(dbId);

                if (state == null) {
                    log.info("--- [DEBUG] Датчик сценария ID='{}' НЕ НАЙДЕН в снапшоте ---", dbId);
                    allConditionsMatch = false;
                    break;
                }

                // Проверяем само условие
                if (!matchCondition(sc, state)) {
                    log.info("--- [DEBUG] Условие для датчика '{}' НЕ ВЫПОЛНЕНО ---", dbId);
                    allConditionsMatch = false;
                    break;
                }
            }

            if (allConditionsMatch && !scenario.getConditions().isEmpty()) {
                log.info("--- [DEBUG] Сценарий '{}' ВЫПОЛНЕН! Отправляем сигнал ---", scenario.getName());
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

        int actualValue;
        if (value instanceof Boolean b) {
            actualValue = b ? 1 : 0;
        } else if (value instanceof Number n) {
            actualValue = n.intValue();
        } else {
            String strValue = value.toString().toLowerCase();
            if (strValue.equals("true") || strValue.equals("on") || strValue.equals("active") || strValue.equals("open")) {
                actualValue = 1;
            } else if (strValue.equals("false") || strValue.equals("off") || strValue.equals("inactive") || strValue.equals("closed")) {
                actualValue = 0;
            } else {
                try {
                    actualValue = Integer.parseInt(strValue);
                } catch (NumberFormatException e) {
                    log.warn("Не удалось преобразовать строковое значение '{}' к числу", strValue);
                    return false;
                }
            }
        }

        int target = sc.getCondition().getValue();
        boolean result = switch (sc.getCondition().getOperation()) {
            case EQUALS -> actualValue == target;
            case GREATER_THAN -> actualValue > target;
            case LOWER_THAN -> actualValue < target;
        };

        log.info("DEBUG MATCH: Датчик {} | Извлечено: {} | Цель: {} | Операция: {} | Итог: {}",
                sc.getId().getSensorId(), actualValue, target, sc.getCondition().getOperation(), result);
        return result;
    }
}