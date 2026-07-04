package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.CartItemDto;
import ru.yandex.practicum.dto.ProductDto;

import java.util.List;

public interface CartService {
    CartDto getCart(String resolvedUid);
    CartDto changeQuantity(String resolvedUid, String productId, Integer quantity);
    void removeItem(String resolvedUid, String productId);
    void updateProductQuantity(String userId, String productId, int quantity);
}