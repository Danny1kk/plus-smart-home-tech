package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.model.ShoppingCart;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShoppingCartMapper {
    ShoppingCart mapToCart(CartDto shoppingCartDto);

    CartDto mapToCartDto(ShoppingCart shoppingCart);
}