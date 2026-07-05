package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.dto.warehouse.AddToCartRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedDto;
import ru.yandex.practicum.dto.warehouse.WarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

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
}