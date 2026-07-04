package ru.yandex.practicum.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.CartDto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CartRepository {

    private final Map<String, CartDto> storage = new ConcurrentHashMap<>();

    public CartDto getCart(String userId) {
        return storage.computeIfAbsent(userId, uid -> new CartDto(uid, new HashMap<>(), true));
    }

    public CartDto changeQuantity(String userId, String productId, Long quantity) {
        CartDto cart = getCart(userId);
        Map<String, Long> products = cart.getProducts();

        if (quantity == null || quantity <= 0) {
            products.remove(productId);
        } else {
            products.put(productId, quantity);
        }

        return cart;
    }

    public void removeItem(String userId, String productId) {
        CartDto cart = storage.get(userId);
        if (cart != null && cart.getProducts() != null) {
            cart.getProducts().remove(productId);
        }
    }

    public void deactivate(String username) {
        CartDto cart = storage.get(username);
        if (cart != null) {
            cart.setActive(false);
        }
    }
}