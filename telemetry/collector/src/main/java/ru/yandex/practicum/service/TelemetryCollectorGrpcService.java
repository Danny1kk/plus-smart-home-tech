package ru.yandex.practicum.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import ru.yandex.practicum.config.KafkaEventProducer;
import ru.yandex.practicum.config.ProducerParam;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.EventMessage;

import java.nio.ByteBuffer;

@Slf4j
@GrpcService
public class TelemetryCollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final KafkaEventProducer kafkaEventProducer;
    private final String sensorsTopic;
    private final String hubsTopic;

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
        try {
            EventMessage eventMessage = EventMessage.newBuilder()
                    .setSensorEvent(request)
                    .build();

            byte[] rawData = eventMessage.toByteArray();
            byte[] dataWithLength = addLengthPrefix(rawData);

            ProducerParam param = ProducerParam.builder()
                    .topic(sensorsTopic)
                    .key(request.getId())
                    .value(dataWithLength)
                    .timestamp(request.getTimestamp().getSeconds() * 1000)
                    .eventClass("SensorEventProto")
                    .eventType("SENSOR_EVENT")
                    .build();

            kafkaEventProducer.sendRecord(param);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке события датчика: {}", request.getId(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие хаба: {}", request.getHubId());
        try {
            EventMessage eventMessage = EventMessage.newBuilder()
                    .setHubEvent(request)
                    .build();

            byte[] rawData = eventMessage.toByteArray();
            byte[] dataWithLength = addLengthPrefix(rawData);

            ProducerParam param = ProducerParam.builder()
                    .topic(hubsTopic)
                    .key(request.getHubId())
                    .value(dataWithLength)
                    .timestamp(request.getTimestamp().getSeconds() * 1000)
                    .eventClass("HubEventProto")
                    .eventType("HUB_EVENT")
                    .build();

            kafkaEventProducer.sendRecord(param);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке события хаба: {}", request.getHubId(), e);
            responseObserver.onError(e);
        }
    }

    private byte[] addLengthPrefix(byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
        buffer.putInt(data.length);
        buffer.put(data);
        return buffer.array();
    }
}