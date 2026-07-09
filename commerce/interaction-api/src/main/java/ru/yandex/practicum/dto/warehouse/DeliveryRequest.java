package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryRequest {
    @NotNull(message = "orderId не может быть NULL.")
    UUID orderId;

    @NotNull(message = "deliveryId не может быть NULL.")
    UUID deliveryId;
}