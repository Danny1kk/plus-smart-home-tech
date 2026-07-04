package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.repository.CartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public CartDto changeQuantity(String resolvedUid, UUID productId, Long quantity) {
        if (quantity == null || quantity <= 0) {
            removeItem(resolvedUid, productId);
            return getCart(resolvedUid);
        }

        Long pid = Long.valueOf(productId.hashCode());

        Boolean isAvailable = warehouseClient.checkAndReserveItems(Map.of(pid, quantity.intValue()));
        if (Boolean.FALSE.equals(isAvailable)) {
            throw new IllegalArgumentException("Недостаточно товара на складе");
        }

        return cartRepository.changeQuantity(resolvedUid, productId, quantity.intValue());    }

    public void removeItem(String resolvedUid, UUID productId) {
        cartRepository.removeItem(resolvedUid, productId);
    }

    @Override
    public void updateProductQuantity(String userId, String productId, int quantity) {
        changeQuantity(userId, UUID.fromString(productId), (long) quantity);
    }

    @Override
    public CartDto removeProducts(String username, List<UUID> ids) {
        for (UUID id : ids) {
            removeItem(username, id);
        }
        return getCart(username);
    }

    @Override
    public CartDto addProducts(String username, Map<UUID, Long> products) {
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            changeQuantity(username, entry.getKey(), entry.getValue());
        }
        return getCart(username);
    }

    @Override
    public void deactivate(String username) {
    }
}