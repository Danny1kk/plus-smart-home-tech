//package ru.yandex.practicum.service;
//
//import com.google.protobuf.Empty;
//import io.grpc.Status;
//import io.grpc.stub.StreamObserver;
//import lombok.extern.slf4j.Slf4j;
//import net.devh.boot.grpc.server.service.GrpcService;
//import org.springframework.beans.factory.annotation.Value;
//import ru.yandex.practicum.config.KafkaEventProducer;
//import ru.yandex.practicum.config.ProducerParam;
//import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
//import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
//import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Slf4j
//@GrpcService
//public class TelemetryCollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {
//
//    private final KafkaEventProducer kafkaEventProducer;
//    private final String sensorsTopic;
//    private final String hubsTopic;
//
//    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
//
//    public TelemetryCollectorGrpcService(
//            KafkaEventProducer kafkaEventProducer,
//            @Value("${kafka.topic.telemetry.sensors-topic}") String sensorsTopic,
//            @Value("${kafka.topic.telemetry.hubs-topic}") String hubsTopic) {
//        this.kafkaEventProducer = kafkaEventProducer;
//        this.sensorsTopic = sensorsTopic;
//        this.hubsTopic = hubsTopic;
//    }
//
//    @Override
//    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
//        log.trace("gRPC: Получено событие датчика: {}", request.getId());
//
//        responseObserver.onNext(Empty.getDefaultInstance());
//        responseObserver.onCompleted();
//
//        executorService.submit(() -> {
//            try {
//                byte[] data = request.toByteArray();
//
//                long timestamp = request.getTimestamp().getSeconds() > 0
//                        ? request.getTimestamp().getSeconds() * 1000
//                        : System.currentTimeMillis();
//                String eventClass = request.getClass().getName();
//
//                ProducerParam param = ProducerParam.builder()
//                        .topic(sensorsTopic)
//                        .key(request.getId())
//                        .value(data)
//                        .timestamp(timestamp)
//                        .eventClass(eventClass)
//                        .eventType(request.getPayloadCase().toString())
//                        .build();
//
//                kafkaEventProducer.sendRecord(param);
//
//            } catch (Exception e) {
//                log.error("Ошибка при асинхронной обработке gRPC события датчика", e);
//            }
//        });
//    }
//
//    @Override
//    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
//        log.trace("gRPC: Получено событие хаба: {}", request.getHubId());
//
//        responseObserver.onNext(Empty.getDefaultInstance());
//        responseObserver.onCompleted();
//
//        executorService.submit(() -> {
//            try {
//                byte[] data = request.toByteArray();
//
//                long timestamp = request.getTimestamp().getSeconds() > 0
//                        ? request.getTimestamp().getSeconds() * 1000
//                        : System.currentTimeMillis();
//
//                String eventClass = request.getClass().getName();
//                ProducerParam param = ProducerParam.builder()
//                        .topic(hubsTopic)
//                        .key(request.getHubId())
//                        .value(data)
//                        .timestamp(timestamp)
//                        .eventClass(eventClass)
//                        .eventType(request.getPayloadCase().toString())
//                        .build();
//
//                kafkaEventProducer.sendRecord(param);
//
//            } catch (Exception e) {
//                log.error("Ошибка при асинхронной обработке события хаба: {}", request.getHubId(), e);
//                responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asException());
//            }
//        });
//    }
//}

package ru.yandex.practicum.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.beans.factory.annotation.Value;
import ru.yandex.practicum.config.KafkaEventProducer;
import ru.yandex.practicum.config.ProducerParam;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

import ru.yandex.practicum.kafka.telemetry.event.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class TelemetryCollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final KafkaEventProducer kafkaEventProducer;
    private final String sensorsTopic;
    private final String hubsTopic;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public TelemetryCollectorGrpcService(
            KafkaEventProducer kafkaEventProducer,
            @Value("${kafka.topic.telemetry.sensors-topic}") String sensorsTopic,
            @Value("${kafka.topic.telemetry.hubs-topic}") String hubsTopic) {
        this.kafkaEventProducer = kafkaEventProducer;
        this.sensorsTopic = sensorsTopic;
        this.hubsTopic = hubsTopic;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие датчика: {}", request.getId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();

        executorService.submit(() -> {
            try {
                long timestamp = request.getTimestamp().getSeconds() > 0
                        ? (request.getTimestamp().getSeconds() * 1000) + (request.getTimestamp().getNanos() / 1_000_000)
                        : java.time.Instant.now().toEpochMilli();

                SensorEventAvro avroEvent = SensorEventAvro.newBuilder()
                        .setId(request.getId())
                        .setHubId(request.getHubId())
                        .setTimestamp(Instant.ofEpochMilli(timestamp))
                        .setPayload(mapSensorPayload(request))
                        .build();

                byte[] data = serializeAvro(avroEvent);

                String eventClass = avroEvent.getClass().getName();

                ProducerParam param = ProducerParam.builder()
                        .topic(sensorsTopic)
                        .key(request.getId())
                        .value(data)
                        .timestamp(timestamp)
                        .eventClass(eventClass)
                        .eventType(request.getPayloadCase().toString())
                        .build();

                kafkaEventProducer.sendRecord(param);

            } catch (Exception e) {
                log.error("Ошибка при асинхронной обработке gRPC события датчика", e);
            }
        });
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие хаба: {}", request.getHubId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();

        executorService.submit(() -> {
            try {
                long timestamp = request.getTimestamp().getSeconds() > 0
                        ? (request.getTimestamp().getSeconds() * 1000) + (request.getTimestamp().getNanos() / 1_000_000)
                        : java.time.Instant.now().toEpochMilli();

                HubEventAvro avroHubEvent = HubEventAvro.newBuilder()
                        .setHubId(request.getHubId())
                        .setTimestamp(Instant.ofEpochMilli(timestamp))
                        .setPayload(mapHubPayloadToAvro(request))
                        .build();

                byte[] data = serializeAvro(avroHubEvent);

                String eventClass = avroHubEvent.getClass().getName();

                ProducerParam param = ProducerParam.builder()
                        .topic(hubsTopic)
                        .key(request.getHubId())
                        .value(data)
                        .timestamp(timestamp)
                        .eventClass(eventClass)
                        .eventType(request.getPayloadCase().toString())
                        .build();

                kafkaEventProducer.sendRecord(param);

            } catch (Exception e) {
                log.error("Ошибка при асинхронной обработке события хаба: {}", request.getHubId(), e);
            }
        });
    }

    private Object mapHubPayloadToAvro(HubEventProto request) {
        return switch (request.getPayloadCase()) {
            case DEVICE_ADDED -> DeviceAddedEventAvro.newBuilder()
                    .setId(request.getDeviceAdded().getId())
                    .setType(DeviceTypeAvro.valueOf(request.getDeviceAdded().getType().name()))
                    .build();

            case DEVICE_REMOVED -> DeviceRemovedEventAvro.newBuilder()
                    .setId(request.getDeviceRemoved().getId())
                    .build();

            case SCENARIO_ADDED -> {
                List<ScenarioConditionAvro> conditions = request.getScenarioAdded().getConditionsList().stream()
                        .map(condition -> ScenarioConditionAvro.newBuilder()
                                .setSensorId(condition.getSensorId())
                                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                                .setValue(condition.hasBoolValue()
                                        ? condition.getBoolValue()
                                        : condition.getIntValue())
                                .build())
                        .collect(Collectors.toList());

                List<DeviceActionAvro> actions = request.getScenarioAdded().getActionsList().stream()
                        .map(action -> DeviceActionAvro.newBuilder()
                                .setSensorId(action.getSensorId())
                                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                                .setValue(action.hasValue() ? action.getValue() : null)
                                .build())
                        .collect(Collectors.toList());

                yield ScenarioAddedEventAvro.newBuilder()
                        .setName(request.getScenarioAdded().getName())
                        .setConditions(conditions)
                        .setActions(actions)
                        .build();
            }

            case SCENARIO_REMOVED -> ScenarioRemovedEventAvro.newBuilder()
                    .setName(request.getScenarioRemoved().getName())
                    .build();

            default -> throw new IllegalArgumentException("Неизвестное событие хаба: " + request.getPayloadCase());
        };
    }

    private Object mapSensorPayload(SensorEventProto request) {
        return switch (request.getPayloadCase()) {
            case MOTION_SENSOR_EVENT -> MotionSensorAvro.newBuilder()
                    .setLinkQuality(request.getMotionSensorEvent().getLinkQuality())
                    .setMotion(request.getMotionSensorEvent().getMotion())
                    .setVoltage(request.getMotionSensorEvent().getVoltage())
                    .build();
            case TEMPERATURE_SENSOR_EVENT -> TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(request.getTemperatureSensorEvent().getTemperatureC())
                    .setTemperatureF(request.getTemperatureSensorEvent().getTemperatureF())
                    .build();
            case LIGHT_SENSOR_EVENT -> LightSensorAvro.newBuilder()
                    .setLinkQuality(request.getLightSensorEvent().getLinkQuality())
                    .setLuminosity(request.getLightSensorEvent().getLuminosity())
                    .build();
            case CLIMATE_SENSOR_EVENT -> ClimateSensorAvro.newBuilder()
                    .setTemperatureC(request.getClimateSensorEvent().getTemperatureC())
                    .setHumidity(request.getClimateSensorEvent().getHumidity())
                    .setCo2Level(request.getClimateSensorEvent().getCo2Level())
                    .build();
            case SWITCH_SENSOR_EVENT -> SwitchSensorAvro.newBuilder()
                    .setState(request.getSwitchSensorEvent().getState())
                    .build();
            default -> throw new IllegalArgumentException("Неизвестный тип датчика: " + request.getPayloadCase());
        };
    }

    private <T extends org.apache.avro.specific.SpecificRecordBase> byte[] serializeAvro(T avroObject) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(avroObject.getSchema());
        writer.write(avroObject, encoder);
        encoder.flush();
        return out.toByteArray();
    }
}