package ru.yandex.practicum.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.model.Action;

import java.time.Instant;

@Component
public class HubRouterClient {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    public void sendAction(String hubId, String scenarioName, Action action, Instant timestamp) {
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
                        .setSensorId(action.getId().toString())
                        .build())
                .build();

        hubRouterStub.handleDeviceAction(request);
    }
}