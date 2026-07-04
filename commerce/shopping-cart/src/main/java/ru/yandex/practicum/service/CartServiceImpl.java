package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.repository.CartRepository;

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
    public CartDto changeQuantity(String resolvedUid, String productId, Integer quantity) {
        Long pid = Long.parseLong(productId);
        Boolean isAvailable = warehouseClient.checkAndReserveItems(Map.of(pid, quantity));

        if (Boolean.FALSE.equals(isAvailable)) {
            throw new IllegalArgumentException("Недостаточно товара на складе");
        }

        return cartRepository.changeQuantity(resolvedUid, productId, quantity);
    }

    @Override
    public void removeItem(String resolvedUid, String productId) {
        cartRepository.removeItem(resolvedUid, productId);
    }
}