package ru.yandex.practicum.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorAppGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@GrpcService
public class TelemetryCollectorGrpcService extends CollectorAppGrpc.CollectorAppImplBase {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final String sensorsTopic;
    private final String hubsTopic;

    public TelemetryCollectorGrpcService(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            @Value("${kafka.topic.telemetry.sensors-topic}") String sensorsTopic,
            @Value("${kafka.topic.telemetry.hubs-topic}") String hubsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.sensorsTopic = sensorsTopic;
        this.hubsTopic = hubsTopic;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие датчика: {}", request.getId());
        try {
            byte[] payload = request.toByteArray();
            kafkaTemplate.send(sensorsTopic, request.getId(), payload);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие хаба: {}", request.getHubId());
        try {
            byte[] payload = request.toByteArray();
            kafkaTemplate.send(hubsTopic, request.getHubId(), payload);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}