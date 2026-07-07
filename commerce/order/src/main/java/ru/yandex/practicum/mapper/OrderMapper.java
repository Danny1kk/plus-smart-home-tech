package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.model.Order;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    Order mapToOrder(OrderDto orderDto);

    OrderDto mapToOrderDto(Order order);
}