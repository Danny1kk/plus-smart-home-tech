package ru.yandex.practicum.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.model.Action;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HubRouterClient {

    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    public HubRouterClient(@Value("${grpc.client.hub-router.address}") String actionHandlerAddress) {
        String target = actionHandlerAddress.replace("static://", "");
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .keepAliveTime(5, TimeUnit.MINUTES)
                .keepAliveTimeout(30, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();
        this.hubRouterStub = HubRouterControllerGrpc.newBlockingStub(channel);
    }

    public void sendAction(String hubId, String scenarioName, String sensorId, Action action, Instant timestamp) {
        ActionTypeProto protoType = ActionTypeProto.valueOf(action.getType().name());

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano())
                        .build())
                .setAction(ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto.newBuilder()
                        .setType(protoType)
                        .setValue(action.getValue())
                        .setSensorId(sensorId)
                        .build())
                .build();

        sendAction(request);
    }

    public void sendAction(DeviceActionRequest request) {
        try {
            log.info("Отправка команды в Hub Router для хаба {} и устройства {}", request.getHubId(), request.getScenarioName());
            hubRouterStub.handleDeviceAction(request);
        } catch (Exception e) {
            log.error("Ошибка при отправке gRPC запроса в Hub Router: {}", e.getMessage(), e);
        }
    }
}