package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;

import java.util.UUID;

public interface DeliveryService {

    DeliveryDto createNewDelivery(DeliveryDto deliveryDto);

    void successfulDelivery(UUID deliveryId);

    void pickedProductsInDelivery(UUID deliveryId);

    void failedDelivery(UUID deliveryId);

    Double costDelivery(OrderDto orderDto);
}