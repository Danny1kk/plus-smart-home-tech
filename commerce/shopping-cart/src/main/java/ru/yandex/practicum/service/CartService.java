package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.ProductRequest;
import ru.yandex.practicum.dto.cart.CartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CartService {
    CartDto getShoppingCart(String username);

    CartDto addProductInCart(String username, Map<UUID, Integer> products);

    void deactivationShoppingCart(String username);

    CartDto removeProductFromCart(String username, List<UUID> productsIds);

    CartDto changeQuantityInCart(String username, ProductRequest quantityRequest);
}