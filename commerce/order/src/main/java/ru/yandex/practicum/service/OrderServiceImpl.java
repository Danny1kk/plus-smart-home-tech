package ru.yandex.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.CreateNewOrder;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturn;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.warehouse.ProductsOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedDto;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exception.cart.NotAuthorizedUserException;
import ru.yandex.practicum.exception.order.NoOrderFoundException;
import ru.yandex.practicum.client.delivery.DeliveryFeignClient;
import ru.yandex.practicum.client.payment.PaymentFeignClient;
import ru.yandex.practicum.client.warehouse.WarehouseClient;
import ru.yandex.practicum.mapper.AddressMapper;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.AddressRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final AddressMapper addressMapper;
    private final OrderMapper orderMapper;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final PaymentFeignClient paymentClient;
    private final DeliveryFeignClient deliveryClient;

    private void checkUsernameForEmpty(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя указанно НЕ ВЕРНО");
        }
    }

    private Order getOrderById(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new NoOrderFoundException(
                String.format("ОШИБКА: ЗАКАЗ с ID = %s НЕ НАЙДЕН", id)));
    }

    private OrderDto toDto(Order order) {
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    public Page<OrderDto> getOrderByUsername(String username, Pageable pageable) {
        checkUsernameForEmpty(username);
        Page<Order> orders = orderRepository.findByUsername(username, pageable);
        return orders.map(orderMapper::mapToOrderDto);
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(String username, CreateNewOrder createOrder) {
        checkUsernameForEmpty(username);

        BookedDto bookedDto = warehouseClient.checkQuantityProducts(createOrder.getShoppingCartDto());
        Address address = addressRepository.save(
                addressMapper.mapToAddress(createOrder.getDeliveryAddress()));

        Order newOrder = Order.builder()
                .shoppingCartId(createOrder.getShoppingCartDto().getCartId())
                .products(createOrder.getShoppingCartDto().getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(bookedDto.getDeliveryWeight())
                .deliveryVolume(bookedDto.getDeliveryVolume())
                .fragile(bookedDto.getFragile())
                .username(username)
                .address(address)
                .build();

        return toDto(orderRepository.save(newOrder));
    }

    @Override
    @Transactional
    public OrderDto paymentOrder(UUID orderId) {
        Order order = getOrderById(orderId);

        if (order.getState() == OrderState.ON_PAYMENT) {
            order.setState(OrderState.PAID);
            return toDto(order);
        }

        if (order.getState() == OrderState.PAID) {
            return toDto(order);
        }

        if (order.getState() != OrderState.ASSEMBLED) {
            throw new ValidationException(
                    String.format("Ваш заказ c ID = %s ещё собирается.", orderId));
        }

        PaymentDto paymentDto = paymentClient.makingPaymentForOrder(toDto(order));
        order.setPaymentId(paymentDto.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);

        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto paymentOrderFailed(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto deliveryOrder(UUID orderId) {
        Order order = getOrderById(orderId);

        if (order.getState() == OrderState.ON_DELIVERY) {
            order.setState(OrderState.DELIVERED);
            return toDto(order);
        }

        if (order.getState() == OrderState.DELIVERED) {
            return toDto(order);
        }

        if (order.getState() != OrderState.PAID) {
            throw new ValidationException(
                    String.format("Доставка осуществляется только по предоплате. Заказ с ID = %s не оплачен.", orderId));
        }

        deliveryClient.pickedProductsInDelivery(order.getDeliveryId());
        order.setState(OrderState.ON_DELIVERY);

        return toDto(order);
    }

    @Override
    @Transactional
    public OrderDto deliveryOrderFailed(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setState(OrderState.DELIVERY_FAILED);

        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto completedOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setState(OrderState.COMPLETED);

        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto calculateOrderTotalPrice(UUID orderId) {
        Order order = getOrderById(orderId);

        Double productsPrice = paymentClient.calculateProductCostPayment(toDto(order));
        order.setProductPrice(productsPrice);

        Double totalPrice = paymentClient.calculateTotalCostPayment(toDto(order));
        order.setTotalPrice(totalPrice);

        return toDto(order);
    }

    @Override
    @Transactional
    public OrderDto calculateOrderDeliveryPrice(UUID orderId) {
        Order order = getOrderById(orderId);

        DeliveryDto deliveryDto = deliveryClient.createNewDelivery(DeliveryDto.builder()
                .fromAddress(warehouseClient.getAddress())
                .toAddress(addressMapper.mapToAddressDto(order.getAddress()))
                .orderId(orderId)
                .deliveryState(DeliveryState.CREATED)
                .build());

        order.setDeliveryId(deliveryDto.getDeliveryId());

        Double deliveryPrice = deliveryClient.costDelivery(toDto(order));
        order.setDeliveryPrice(deliveryPrice);
        return toDto(order);
    }

    @Override
    @Transactional
    public OrderDto assemblyOrder(UUID orderId) {
        Order order = getOrderById(orderId);

        if (order.getState() == OrderState.NEW) {
            warehouseClient.assemblyProductOrderDelivery(ProductsOrderRequest.builder()
                    .products(order.getProducts())
                    .orderId(orderId)
                    .build());
            order.setState(OrderState.ASSEMBLED);
        } else {
            throw new ValidationException(String.format(
                    "ОШИБКА: Заказ с ID = %s отправить на сборку НЕ ВОЗМОЖНО. РЕШЕНИЕ: Создайте новый заказ.", orderId));
        }
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto assemblyOrderFailed(UUID orderId) {
        Order order = getOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);

        return toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto returnOrderProducts(ProductReturn productReturn) {
        Order order = getOrderById(productReturn.getOrderId());

        order.setState(OrderState.PRODUCT_RETURNED);

        return toDto(orderRepository.save(order));
    }
}