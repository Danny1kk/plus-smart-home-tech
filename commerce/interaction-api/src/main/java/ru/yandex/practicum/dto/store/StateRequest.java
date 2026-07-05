package ru.yandex.practicum.dto.store;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.enums.QuantityState;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class StateRequest {
    @NotNull
    UUID productId;

    @NotNull
    QuantityState quantityState;
}