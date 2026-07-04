package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.repository.CartRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final WarehouseClient warehouseClient;

    @Override
    public CartDto getCart(String resolvedUid) {
        return cartRepository.getCart(resolvedUid);
    }

    @Override
    public CartDto changeQuantity(String username, String productId, Long quantity) {
        CartDto cart = cartRepository.getCart(username);

        if (!cart.isActive()) {
            throw new IllegalArgumentException("Cannot modify a deactivated shopping cart.");
        }

        if (quantity == null || quantity <= 0) {
            cartRepository.removeItem(username, productId);
            return cartRepository.getCart(username);
        }

        Long pid = Long.valueOf(productId);
        Boolean isAvailable = warehouseClient.checkAndReserveItems(Map.of(pid, quantity.intValue()));

        if (Boolean.FALSE.equals(isAvailable)) {
            throw new IllegalArgumentException("Недостаточно товара на складе");
        }

        return cartRepository.changeQuantity(username, productId, quantity);
    }

    @Override
    public void updateProductQuantity(String userId, String productId, int quantity) {
        changeQuantity(userId, productId, (long) quantity);
    }

    @Override
    public CartDto removeProducts(String username, List<String> productIds) {
        CartDto cart = cartRepository.getCart(username);
        if (!cart.isActive()) {
            throw new IllegalArgumentException("Cannot modify a deactivated shopping cart.");
        }
        for (String id : productIds) {
            cartRepository.removeItem(username, id);
        }
        return cartRepository.getCart(username);
    }

    @Override
    public CartDto addProducts(String username, Map<String, Long> products) {
        CartDto cart = cartRepository.getCart(username);
        if (!cart.isActive()) {
            throw new IllegalArgumentException("Cannot modify a deactivated shopping cart.");
        }

        for (Map.Entry<String, Long> entry : products.entrySet()) {
            changeQuantity(username, entry.getKey(), entry.getValue());
        }
        return cartRepository.getCart(username);
    }

    @Override
    public void deactivate(String username) {
        cartRepository.deactivate(username);
    }
}