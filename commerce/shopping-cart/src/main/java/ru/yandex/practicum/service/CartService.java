package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.CartDto;

import java.util.List;
import java.util.Map;

public interface CartService {
    CartDto getCart(String resolvedUid);
    CartDto changeQuantity(
            String username,
            String productId,
            Long quantity
    );
    CartDto removeProducts(
            String username,
            List<String> productIds
    );
    void updateProductQuantity(
            String userId,
            String productId,
            int quantity
    );

    CartDto addProducts(
            String username,
            Map<String, Long> products
    );

    void deactivate(String username);
}