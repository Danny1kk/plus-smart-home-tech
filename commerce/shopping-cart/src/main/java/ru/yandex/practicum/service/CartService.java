package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.CartDto;
import java.util.List;

public interface CartService {
    CartDto getCart(String resolvedUid);
    CartDto changeQuantity(String resolvedUid, String productId, Integer quantity);
    void removeItem(String resolvedUid, String productId);
}