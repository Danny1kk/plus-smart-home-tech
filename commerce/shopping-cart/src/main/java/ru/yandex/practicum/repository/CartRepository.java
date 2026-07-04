package ru.yandex.practicum.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.CartDto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CartRepository {

    private final Map<String, CartDto> storage = new ConcurrentHashMap<>();

    public CartDto getCart(String userId) {
        return storage.computeIfAbsent(userId, uid -> new CartDto(UUID.randomUUID(), new HashMap<>()));
    }

    public CartDto changeQuantity(String userId, UUID productId, Integer quantity) {
        CartDto cart = getCart(userId);
        Map<UUID, Long> products = cart.getProducts();

        if (products == null) {
            products = new HashMap<>();
            cart.setProducts(products);
        }

        if (quantity == null || quantity <= 0) {
            products.remove(productId);
        } else {
            products.put(productId, quantity.longValue());
        }

        return cart;
    }

    public void removeItem(String userId, UUID productId) {
        CartDto cart = storage.get(userId);
        if (cart != null && cart.getProducts() != null) {
            cart.getProducts().remove(productId);
        }
    }

    public void deactivate(String username) {
        storage.remove(username);
    }
}