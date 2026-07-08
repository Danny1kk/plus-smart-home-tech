package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.DeliveryRequest;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.exception.delivery.NoDeliveryFoundException;
import ru.yandex.practicum.client.order.OrderFeignClient;
import ru.yandex.practicum.client.warehouse.WarehouseClient;

import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseClient warehouseClient;
    private final OrderFeignClient orderClient;

    @Value("${delivery.base.cost}")
    private Double baseCost;

    private Delivery getDeliveryById(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException(
                        String.format("ОШИБКА: Доставка с ID = %s не найдена", deliveryId)));
    }

    @Override
    @Transactional
    public DeliveryDto createNewDelivery(DeliveryDto deliveryDto) {
        return deliveryMapper.mapToDeliveryDto(deliveryRepository.save(deliveryMapper.mapToDelivery(deliveryDto)));
    }

    @Override
    @Transactional
    public void successfulDelivery(UUID deliveryId) {
        Delivery delivery = getDeliveryById(deliveryId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        orderClient.deliveryOrder(delivery.getOrderId());
    }

    @Override
    @Transactional
    public void pickedProductsInDelivery(UUID deliveryId) {
        Delivery delivery = getDeliveryById(deliveryId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);

        warehouseClient.shippedProductForDelivery(DeliveryRequest.builder()
                .orderId(delivery.getOrderId())
                .deliveryId(deliveryId)
                .build());
    }

    @Override
    @Transactional
    public void failedDelivery(UUID deliveryId) {
        Delivery delivery = getDeliveryById(deliveryId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        orderClient.deliveryOrderFailed(delivery.getOrderId());
    }

//    @Override
//    public Double costDelivery(OrderDto orderDto) {
//        Delivery delivery = getDeliveryById(orderDto.getDeliveryId());
//
//        Address fromAddress = delivery.getFromAddress();
//        Address toAddress = delivery.getToAddress();
//
//        double warehouseMarKup = 1.0;
//        //if (fromAddress.toString().contains("ADDRESS_2")) {
//        if ("ADDRESS_2".equals(fromAddress.getStreet())) {
//            warehouseMarKup = 2.0;
//        }
//        double deliveryCost = baseCost + (baseCost * warehouseMarKup);
//
//        if (orderDto.getFragile()) {
//            deliveryCost += deliveryCost * 0.2;
//        }
//        deliveryCost += orderDto.getDeliveryWeight() * 0.3 + orderDto.getDeliveryVolume() * 0.2;
//
//        if (fromAddress.getStreet() != null && toAddress.getStreet() != null) {
//            String fromStreet = fromAddress.getStreet().trim().toLowerCase();
//            String toStreet = toAddress.getStreet().trim().toLowerCase();
//            if (!fromStreet.equals(toStreet)) {
//                deliveryCost += deliveryCost * 0.2;
//            }
//        } else {
//            throw new IllegalArgumentException("Адрес не может быть null");
//        }
//        return deliveryCost;
//    }

    @Override
    public Double costDelivery(OrderDto orderDto) {
        Delivery delivery = getDeliveryById(orderDto.getDeliveryId());

        Address fromAddress = delivery.getFromAddress();
        Address toAddress = delivery.getToAddress();

        if (fromAddress == null || toAddress == null ||
                fromAddress.getStreet() == null || toAddress.getStreet() == null) {
            throw new IllegalArgumentException("ОШИБКА: Адреса отправления и доставки должны быть заполнены");
        }

        double warehouseMarkup = 1.0;
        if (fromAddress.toString().contains("ADDRESS_2")) {
            warehouseMarkup = 1.2;
        }

        double deliveryCost = 150.0 * warehouseMarkup;
        log.info("Рассчитанная стоимость доставки: {}", deliveryCost);
        return deliveryCost;
    }
}