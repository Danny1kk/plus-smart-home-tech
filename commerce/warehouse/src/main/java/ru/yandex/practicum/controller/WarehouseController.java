package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController {
    private final WarehouseService warehouseService;

    @PutMapping
    public void newProduct(@Valid @RequestBody WarehouseRequest newRequest) {
        log.info("Начинаем добавление нового продукта = {}", newRequest);
        warehouseService.newProduct(newRequest);
        log.info("Новый продукт добавлен УСПЕШНО = {}", newRequest);
    }

    @PostMapping("/check")
    public BookedDto checkProducts(@Valid @RequestBody CartDto cartDto) {
        log.info("Начинаем проверку кол-ва товаров на складе={}", cartDto);
        BookedDto result = warehouseService.checkQuantityProducts(cartDto);
        log.info("Проверка кол-ва товара на складе прошла УСПЕШНО = {}, result = {}", cartDto, result);
        return result;
    }

    @PostMapping("/add")
    public void addProduct(@Valid @RequestBody AddToCartRequest addRequest) {
        log.info("Принимаем товар на склад = {}", addRequest);
        warehouseService.addQuantityProduct(addRequest);
        log.info("Товар принят УСПЕШНО = {}", addRequest);
    }

    @GetMapping("/address")
    public AddressDto getAddress() {
        log.info("Запрашиваем адрес склада");
        AddressDto result = warehouseService.getAddress();
        log.info("Адрес склада  УСПЕШНО предоставлен = {}", result);
        return result;
    }

    @PostMapping("/shipped")
    public void shippedProductForDelivery(@Valid @RequestBody DeliveryRequest shippedRequest) {
        log.info("Перeдаем заказ {} в доставку {}", shippedRequest.getOrderId(), shippedRequest.getDeliveryId());
        warehouseService.shippedProductForDelivery(shippedRequest);
        log.info("Заказ {} передали в доставку {} УСПЕШНО.", shippedRequest.getOrderId(), shippedRequest.getDeliveryId());
    }

    @PostMapping("/return")
    public void returnProductToTheWarehouse(@RequestBody Map<UUID, @NotNull @Positive Integer> products) {
        log.info("Начинаем возврат товара {} на склад.", products);
        warehouseService.returnProductToTheWarehouse(products);
        log.info("Возврат товара {} прошел УСПЕШНО", products);
    }

    @PostMapping("/assembly")
    public BookedDto assemblyProductOnOrderForDelivery(@Valid @RequestBody ProductsOrderRequest assemblyRequest) {
        log.info("Начинаем сборку товара {} к заказу {} для подготовки к отправке.",
                assemblyRequest.getProducts(), assemblyRequest.getOrderId());
        return warehouseService.assemblyProductOnOrderForDelivery(assemblyRequest);
    }
}