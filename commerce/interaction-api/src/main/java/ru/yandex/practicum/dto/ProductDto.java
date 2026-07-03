package ru.yandex.practicum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;

    @JsonProperty("productName")
    private String name;

    private String description;
    private Double price;
    private String quantityState;
    private Integer quantity;
    private String imageSrc;
}