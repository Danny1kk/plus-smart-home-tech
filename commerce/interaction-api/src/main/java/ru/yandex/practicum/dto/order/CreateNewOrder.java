package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateNewOrder {
    @NotNull(message = "shoppingCartDto не может быть NULL.")
    CartDto shoppingCartDto;

    @NotNull(message = "deliveryAddress не может быть NULL.")
    AddressDto deliveryAddress;
}