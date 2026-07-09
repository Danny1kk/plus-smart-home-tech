package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {
    void newProduct(WarehouseRequest newRequest);

    BookedDto checkQuantityProducts(CartDto cartDto);

    void addQuantityProduct(AddToCartRequest addRequest);

    AddressDto getAddress();

    void shippedProductForDelivery(DeliveryRequest shippedRequest);

    void returnProductToTheWarehouse(Map<UUID, Integer> products);

    BookedDto assemblyProductOnOrderForDelivery(ProductsOrderRequest assemblyRequest);
}