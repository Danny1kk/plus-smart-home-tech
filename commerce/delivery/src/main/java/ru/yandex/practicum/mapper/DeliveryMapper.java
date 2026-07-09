package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.dto.delivery.DeliveryDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeliveryMapper {
    Delivery mapToDelivery(DeliveryDto deliveryDto);

    DeliveryDto mapToDeliveryDto(Delivery delivery);

    Address mapAddress(AddressDto addressDto);

    AddressDto mapAddressDto(Address address);
}