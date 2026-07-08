package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.service.DeliveryService;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery")
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PutMapping
    public DeliveryDto createNewDelivery(@Valid @RequestBody DeliveryDto deliveryDto) {
        log.info("Создаем новую доставку {}.", deliveryDto);
        return deliveryService.createNewDelivery(deliveryDto);
    }

    @PostMapping("/successful")
    public void successfulDelivery(@Valid @RequestBody UUID deliveryId) {
        log.info("Запрос на смену статуса доставки на DELIVERED");
        deliveryService.successfulDelivery(deliveryId);
        log.info("Статус доставки c ID {} = DELIVERED.", deliveryId);
    }

    @PostMapping("/picked")
    public void pickedProductsInDelivery(@Valid @RequestBody UUID deliveryId) {
        log.info("Передаем товар в доставку. ID доставки = {}.", deliveryId);
        deliveryService.pickedProductsInDelivery(deliveryId);
        log.info("Товар УСПЕШНО передали в доставку. ID доставки = {}.", deliveryId);
    }

    @PostMapping("/failed")
    public void failedDelivery(@Valid @RequestBody UUID deliveryId) {
        log.info("Запрос на смену статуса доставки на FAILED");
        deliveryService.failedDelivery(deliveryId);
        log.info("Статус доставки c ID {} = FAILED.", deliveryId);
    }

    @PostMapping("/cost")
    public Double costDelivery(@Valid @RequestBody OrderDto orderDto) {
        log.info("Начинаем расчет стоимости доставки.");
        return deliveryService.costDelivery(orderDto);
    }
}