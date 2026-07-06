package ru.yandex.practicum.dto;

import lombok.Data;

@Data
public class StockReceiptDto {
    private Long productId;
    private Integer quantity;
}