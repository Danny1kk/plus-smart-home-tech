package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.util.UUID;

public interface PaymentService {

    PaymentDto makingPaymentForOrder(OrderDto orderDto);

    Double calculateTotalCostPayment(OrderDto orderDto);

    void successfulPayment(UUID paymentId);

    Double calculateProductCostPayment(OrderDto orderDto);

    void failedPayment(UUID paymentId);
}