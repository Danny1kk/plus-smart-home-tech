package ru.yandex.practicum.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ProductRequest {
    @NotNull
    UUID productId;

    @NotNull
    Integer newQuantity;
}