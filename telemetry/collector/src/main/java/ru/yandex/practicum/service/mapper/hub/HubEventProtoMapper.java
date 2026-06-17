package ru.yandex.practicum.service.mapper.hub;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ValueMapping;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.model.hub.ScenarioCondition;
import ru.yandex.practicum.model.hub.ScenarioRemovedEvent;
import ru.yandex.practicum.model.hub.enums.ActionType;
import ru.yandex.practicum.model.hub.enums.ConditionOperation;
import ru.yandex.practicum.model.hub.enums.ConditionType;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HubEventProtoMapper {

    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    @Mapping(target = "deviceType", source = "deviceAddedEventProto.type")
    @ValueMapping(target = "MOTION_SENSOR", source = "UNRECOGNIZED")
    DeviceAddedEvent mapDeviceAddedProtoToModel(HubEventProto request, DeviceAddedEventProto deviceAddedEventProto);

    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    DeviceRemovedEvent mapDeviceRemovedProtoToModel(HubEventProto request, DeviceRemovedEventProto deviceRemovedEventProto);

    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    @Mapping(target = "conditions", source = "scenarioAddedEventProto.conditionsList")
    @Mapping(target = "actions", source = "scenarioAddedEventProto.actionsList")
    ScenarioAddedEvent mapScenarioAddedProtoToModel(HubEventProto request, ScenarioAddedEventProto scenarioAddedEventProto);

    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    ScenarioRemovedEvent mapScenarioRemovedProtoToModel(HubEventProto request, ScenarioRemovedEventProto scenarioRemovedEventProto);

    @Mapping(target = "type", source = "type")
    @Mapping(target = "operation", source = "operation")
    @Mapping(target = "value", expression = "java(mapScenarioConditionProtoValueToModelValue(scenarioConditionProto))")
    ScenarioCondition mapScenarioConditionProtoToModel(ScenarioConditionProto scenarioConditionProto);

    @ValueMapping(target = "ACTIVATE", source = "UNRECOGNIZED")
    ActionType mapActionTypeProtoToModel(ActionTypeProto actionTypeProto);

    @ValueMapping(target = "EQUALS", source = "UNRECOGNIZED")
    ConditionOperation mapConditionOperationProtoToModel(ConditionOperationProto conditionOperationProto);

    @ValueMapping(target = "MOTION", source = "UNRECOGNIZED")
    ConditionType mapConditionTypeProtoToModel(ConditionTypeProto conditionTypeProto);

    @Named("mapScenarioConditionProtoValueToModelValue")
    default Object mapScenarioConditionProtoValueToModelValue(ScenarioConditionProto proto) {
        if (proto.hasBoolValue()) {
            return proto.getBoolValue();
        } else if (proto.hasIntValue()) {
            return proto.getIntValue();
        } else {
            return null;
        }
    }
}