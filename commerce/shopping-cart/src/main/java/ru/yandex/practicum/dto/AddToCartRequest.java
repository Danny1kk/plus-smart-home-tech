package ru.yandex.practicum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequest {
    @JsonProperty("id")
    private String productId;
    private Integer quantity;
}