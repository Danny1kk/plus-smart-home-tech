package ru.yandex.practicum.service;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.service.handler.sensor.BaseSensorHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class TelemetryCollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubMapHandlers;
    private final Map<SensorEventProto.PayloadCase, BaseSensorHandler> sensorMapHandlers;

    public TelemetryCollectorGrpcService(Set<HubEventHandler> hubSetHandlers, Set<BaseSensorHandler> sensorSetHandlers) {
        this.hubMapHandlers = hubSetHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageHubType,
                        Function.identity()
                ));
        this.sensorMapHandlers = sensorSetHandlers.stream()
                .collect(Collectors.toMap(
                        BaseSensorHandler::getMessageSensorType,
                        Function.identity()
                ));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие датчика: {}, тип: {}", request.getId(), request.getPayloadCase());
        try {
            if (sensorMapHandlers.containsKey(request.getPayloadCase())) {
                sensorMapHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                throw new IllegalArgumentException("Не могу найти обработчик для SENSOR " + request.getPayloadCase());
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке события датчика: {}", request.getId(), e);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.trace("gRPC: Получено событие хаба: {}, тип: {}", request.getHubId(), request.getPayloadCase());
        try {
            if (hubMapHandlers.containsKey(request.getPayloadCase())) {
                hubMapHandlers.get(request.getPayloadCase()).handle(request);
            } else {
                throw new IllegalArgumentException("Не могу найти обработчик для HUB " + request.getPayloadCase());
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке события хаба: {}", request.getHubId(), e);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}