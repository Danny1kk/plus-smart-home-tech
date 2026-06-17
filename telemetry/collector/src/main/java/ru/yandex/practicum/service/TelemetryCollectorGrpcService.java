package ru.yandex.practicum.service;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import ru.yandex.practicum.config.KafkaEventProducer;
import ru.yandex.practicum.config.ProducerParam;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                byte[] data;
                switch (request.getPayloadCase()) {
                    case MOTION_SENSOR_EVENT -> data = request.getMotionSensorEvent().toByteArray();
                    case TEMPERATURE_SENSOR_EVENT -> data = request.getTemperatureSensorEvent().toByteArray();
                    case LIGHT_SENSOR_EVENT -> data = request.getLightSensorEvent().toByteArray();
                    case CLIMATE_SENSOR_EVENT -> data = request.getClimateSensorEvent().toByteArray();
                    case SWITCH_SENSOR_EVENT -> data = request.getSwitchSensorEvent().toByteArray();
                    default -> throw new IllegalArgumentException("Неизвестный тип события датчика: " + request.getPayloadCase());
                }

                long timestamp = request.getTimestamp().getSeconds() > 0
                        ? request.getTimestamp().getSeconds() * 1000
                        : System.currentTimeMillis();

                String eventClass = request.getClass().getSimpleName().replace("Proto", "");

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
                byte[] data;
                switch (request.getPayloadCase()) {
                    case DEVICE_ADDED -> data = request.getDeviceAdded().toByteArray();
                    case DEVICE_REMOVED -> data = request.getDeviceRemoved().toByteArray();
                    case SCENARIO_ADDED -> data = request.getScenarioAdded().toByteArray();
                    case SCENARIO_REMOVED -> data = request.getScenarioRemoved().toByteArray();
                    default -> throw new IllegalArgumentException("Неизвестный тип события хаба: " + request.getPayloadCase());
                }

                long timestamp = request.getTimestamp().getSeconds() > 0
                        ? request.getTimestamp().getSeconds() * 1000
                        : System.currentTimeMillis();

                String eventClass = request.getClass().getSimpleName().replace("Proto", "");

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
}