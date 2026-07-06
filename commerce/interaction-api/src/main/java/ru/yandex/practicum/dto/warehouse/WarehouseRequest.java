package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseRequest {

    @NotNull
    UUID productId;

    Boolean fragile;

    @NotNull
    DimensionDto dimension;

    @Min(1)
    @NotNull
    Double weight;
}