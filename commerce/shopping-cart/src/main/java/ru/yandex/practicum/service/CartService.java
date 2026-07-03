package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.CartDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CartService {

    private final Map<String, Map<String, Integer>> carts = new ConcurrentHashMap<>();

    public CartDto getCart(String userId) {
        Map<String, Integer> items = carts.getOrDefault(userId, new ConcurrentHashMap<>());
        return new CartDto(userId, items);
    }

    public CartDto addItem(String userId, String productId, Integer quantity) {
        Map<String, Integer> userItems = carts.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        userItems.put(productId, userItems.getOrDefault(productId, 0) + quantity);
        return new CartDto(userId, userItems);
    }

    public void clearCart(String userId) {
        carts.remove(userId);
    }

    public CartDto removeItem(String userId, String productId) {
        Map<String, Integer> userItems = carts.get(userId);
        if (userItems != null) {
            userItems.remove(productId);
        }
        return getCart(userId);
    }

    public CartDto changeQuantity(String userId, String productId, Integer quantity) {
        Map<String, Integer> userItems = carts.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        userItems.put(productId, quantity);
        return new CartDto(userId, userItems);
    }
}