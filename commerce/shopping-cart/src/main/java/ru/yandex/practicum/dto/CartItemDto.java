package ru.yandex.practicum.dto;

import ru.yandex.practicum.dto.store.ProductDto;

public class CartItemDto {
    private ProductDto productId;
    private Integer quantity;

    public CartItemDto() {
    }

    public CartItemDto(ProductDto productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public ProductDto getProductId() {
        return productId;
    }

    public void setProductId(ProductDto productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}