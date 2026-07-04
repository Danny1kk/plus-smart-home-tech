package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.CartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CartService {
    CartDto getCart(String resolvedUid);
    CartDto changeQuantity(
            String username,
            UUID productId,
            Long quantity
    );
    CartDto removeProducts(
            String username,
            List<UUID> ids
    );
    void updateProductQuantity(
            String userId,
            String productId,
            int quantity
    );

    CartDto addProducts(
            String username,
            Map<UUID, Long> products
    );

    void deactivate(String username);
}