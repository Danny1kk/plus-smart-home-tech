package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {

    private String shoppingCartId;
    private Map<String, Long> products;
    private boolean active = true;
}