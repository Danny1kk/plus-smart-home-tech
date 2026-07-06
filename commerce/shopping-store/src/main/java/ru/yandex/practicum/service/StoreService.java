package ru.yandex.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.ProductPageDto;
import ru.yandex.practicum.dto.store.StateRequest;
import ru.yandex.practicum.enums.ProductCategory;

import java.util.UUID;

public interface StoreService {
    ProductPageDto getAllProducts(ProductCategory category, Pageable pageable);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean removeProductById(UUID productId);

    Boolean setProductQuantityState(StateRequest stateRequest);

    ProductDto getProductById(UUID productId);
}