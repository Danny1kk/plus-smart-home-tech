package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.model.ShoppingCart;
import org.mapstruct.ReportingPolicy;

//@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShoppingCartMapper {
    ShoppingCart mapToCart(CartDto shoppingCartDto);

    CartDto mapToCartDto(ShoppingCart shoppingCart);
}