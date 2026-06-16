package ru.yandex.practicum.service;

import com.google.protobuf.Empty;
import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import java.nio.charset.StandardCharsets;

@Slf4j
@GrpcService
public class TelemetryCollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

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
            String jsonPayload = JsonFormat.printer().print(request);
            byte[] payload = jsonPayload.getBytes(StandardCharsets.UTF_8);

            kafkaTemplate.send(sensorsTopic, request.getId(), payload);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке события датчика", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие хаба: {}", request.getHubId());
        try {
            String jsonPayload = JsonFormat.printer().print(request);
            byte[] payload = jsonPayload.getBytes(StandardCharsets.UTF_8);

            kafkaTemplate.send(hubsTopic, request.getHubId(), payload);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке события хаба", e);
            responseObserver.onError(e);
        }
    }
}