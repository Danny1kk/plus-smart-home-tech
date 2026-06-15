package ru.yandex.practicum.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.*;

import java.util.List;

public class HubEventMapper {

    public static HubEventAvro toHubEventAvro(HubEvent hubEvent) {

        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(hubEvent.getTimestamp())
                .setPayload(configureHubEventPayload(hubEvent))
                .build();
    }

    private static SpecificRecordBase configureHubEventPayload(HubEvent hubEvent) {
        switch (hubEvent.getType()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent deviceAddedEvent = (DeviceAddedEvent) hubEvent;
                return DeviceAddedEventAvro.newBuilder()
                        .setId(deviceAddedEvent.getId())
                        .setType(DeviceTypeAvro.valueOf(deviceAddedEvent.getDeviceType().name()))
                        .build();
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent deviceRemovedEvent = (DeviceRemovedEvent) hubEvent;
                return DeviceRemovedEventAvro.newBuilder()
                        .setId(deviceRemovedEvent.getHubId())
                        .build();
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) hubEvent;
                List<DeviceActionAvro> deviceActionsAvro = scenarioAddedEvent.getActions().stream()
                        .map(HubEventMapper::toDeviceActionAvro)
                        .toList();
                List<ScenarioConditionAvro> scenarioConditionsAvro = scenarioAddedEvent.getConditions().stream()
                        .map(HubEventMapper::toScenarioConditionAvro)
                        .toList();
                return ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioAddedEvent.getName())
                        .setConditions(scenarioConditionsAvro)
                        .setActions(deviceActionsAvro)
                        .build();
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent scenarioRemovedEvent = (ScenarioRemovedEvent) hubEvent;
                return ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemovedEvent.getName())
                        .build();
            }
            default -> throw new IllegalStateException("payload is wrong");
        }
    }

    private static DeviceActionAvro toDeviceActionAvro(DeviceAction deviceAction) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(deviceAction.getSensorId())
                .setType(ActionTypeAvro.valueOf(deviceAction.getType().name()))
                .setValue(deviceAction.getValue())
                .build();
    }

    private static ScenarioConditionAvro toScenarioConditionAvro(ScenarioCondition scenarioCondition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(scenarioCondition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(scenarioCondition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(scenarioCondition.getOperation().name()))
                .setValue(scenarioCondition.getValue())
                .build();
    }
}