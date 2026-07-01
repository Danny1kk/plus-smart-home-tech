package ru.yandex.practicum.service.mapper.sensor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface SensorEventProtoMapper {

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "hubId", source = "event.hubId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()))")
    @Mapping(target = "payload", source = "event.climateSensorEvent")
    SensorEventAvro toClimateSensorEventAvro(SensorEventProto event);

    ClimateSensorAvro mapPayload(ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto value);
}