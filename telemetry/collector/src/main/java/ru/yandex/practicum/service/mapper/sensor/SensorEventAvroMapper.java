package ru.yandex.practicum.service.mapper.sensor;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.model.sensor.LightSensorEvent;
import ru.yandex.practicum.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.model.sensor.TemperatureSensorEvent;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SensorEventAvroMapper {

    ClimateSensorAvro mapClimateSensorToAvro(ClimateSensorEvent climateSensorEvent);

    LightSensorAvro mapLightSensorToAvro(LightSensorEvent lightSensorEvent);

    MotionSensorAvro mapMotionSensorToAvro(MotionSensorEvent motionSensorEvent);

    SwitchSensorAvro mapSwitchSensorToAvro(SwitchSensorEvent switchSensorEvent);

    TemperatureSensorAvro mapTemperatureSensor(TemperatureSensorEvent temperatureSensorEvent);
}