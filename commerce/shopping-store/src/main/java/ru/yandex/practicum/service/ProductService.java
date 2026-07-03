package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ProductDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final Map<Long, ProductDto> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<ProductDto> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public ProductDto addProduct(ProductDto productDto) {
        long id = idGenerator.getAndIncrement();
        productDto.setId(id);
        products.put(id, productDto);
        return productDto;
    }

    public void checkQuantityState(Long productId, String quantityState) {
        ProductDto product = products.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Товар с ID " + productId + " не найден");
        }

        product.setQuantityState(quantityState);
        products.put(productId, product);
    }

    public ProductDto getProductById(Long id) {
        ProductDto product = products.get(id);
        if (product == null) {
            throw new IllegalArgumentException("Товар с ID " + id + " не найден");
        }
        return product;
    }
}