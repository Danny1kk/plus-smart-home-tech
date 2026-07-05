package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.dto.warehouse.AddToCartRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedDto;
import ru.yandex.practicum.dto.warehouse.WarehouseRequest;

public interface WarehouseService {
    void newProduct(WarehouseRequest newRequest);

    BookedDto checkQuantityProducts(CartDto cartDto);

    void addQuantityProduct(AddToCartRequest addRequest);

    AddressDto getAddress();
}