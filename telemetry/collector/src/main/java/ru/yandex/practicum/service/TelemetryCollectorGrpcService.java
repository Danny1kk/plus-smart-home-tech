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

        executorService.submit(() -> {
            try {
                byte[] data = request.toByteArray();
                String eventClass = request.getClass().getSimpleName().replace("Proto", "");

                ProducerParam param = ProducerParam.builder()
                        .topic(sensorsTopic)
                        .key(request.getId())
                        .value(data)
                        .timestamp(request.getTimestamp() != null ? request.getTimestamp().getSeconds() * 1000 : System.currentTimeMillis())
                        .eventClass(eventClass)
                        .eventType(request.getPayloadCase().toString())
                        .build();

                kafkaEventProducer.sendRecord(param);

                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Ошибка при асинхронной обработке gRPC события датчика", e);
                responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asException());
            }
        });
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие хаба: {}", request.getHubId());

        executorService.submit(() -> {
            try {
                byte[] data = request.toByteArray();
                String eventClass = request.getClass().getSimpleName().replace("Proto", "");

                ProducerParam param = ProducerParam.builder()
                        .topic(hubsTopic)
                        .key(request.getHubId())
                        .value(data)
                        .timestamp(request.getTimestamp() != null ? request.getTimestamp().getSeconds() * 1000 : System.currentTimeMillis())
                        .eventClass(eventClass)
                        .eventType(request.getPayloadCase().toString())
                        .build();

                kafkaEventProducer.sendRecord(param);

                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Ошибка при асинхронной обработке события хаба: {}", request.getHubId(), e);
                responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asException());
            }
        });
    }
}