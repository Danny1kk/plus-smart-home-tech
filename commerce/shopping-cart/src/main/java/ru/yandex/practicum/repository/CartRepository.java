package ru.yandex.practicum.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.CartItemDto;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CartRepository {

    private final Map<String, CartDto> storage = new ConcurrentHashMap<>();

    public CartDto getCart(String userId) {
        return storage.computeIfAbsent(userId, uid -> new CartDto(new ArrayList<>()));
    }

    public CartDto changeQuantity(String userId, String productId, Integer quantity) {
        CartDto cart = getCart(userId);

        CartItemDto existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            if (quantity <= 0) {
                cart.getItems().remove(existingItem);
            } else {
                existingItem.setQuantity(quantity);
            }
        } else if (quantity > 0) {
            cart.getItems().add(new CartItemDto(productId, quantity));
        }

        return cart;
    }

    public void removeItem(String userId, String productId) {
        CartDto cart = storage.get(userId);
        if (cart != null && cart.getItems() != null) {
            cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        }
    }
}