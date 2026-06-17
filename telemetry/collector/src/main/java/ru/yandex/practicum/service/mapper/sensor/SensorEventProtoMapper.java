package ru.yandex.practicum.service.mapper.sensor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.model.sensor.LightSensorEvent;
import ru.yandex.practicum.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.model.sensor.TemperatureSensorEvent;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SensorEventProtoMapper {

    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    ClimateSensorEvent mapClimateSensorProtoToModel(SensorEventProto request, ClimateSensorProto climateSensorProto);

    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    LightSensorEvent mapLightSensorProtoToModel(SensorEventProto request, LightSensorProto lightSensorProto);

    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    MotionSensorEvent mapMotionSensorProtoToModel(SensorEventProto request, MotionSensorProto motionSensorProto);

    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    SwitchSensorEvent mapSwitchSensorProtoToModel(SensorEventProto request, SwitchSensorProto switchSensorProto);

    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "hubId", source = "request.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(request.getTimestamp().getSeconds()))")
    TemperatureSensorEvent mapTemperatureSensorProtoToModel(SensorEventProto request, TemperatureSensorProto temperatureSensorProto);
}