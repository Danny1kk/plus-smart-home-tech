package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentDto makingPaymentForOrder(@Valid @RequestBody OrderDto orderDto) {
        log.info("Формируем оплату по заказу с ID = {}", orderDto.getOrderId());
        return paymentService.makingPaymentForOrder(orderDto);
    }

    @PostMapping("/totalCost")
    public Double calculateTotalCostPayment(@Valid @RequestBody OrderDto orderDto) {
        log.info("Производим расчет полной стоимости заказа с ID = {}.", orderDto.getOrderId());
        return paymentService.calculateTotalCostPayment(orderDto);
    }

    @PostMapping("/refund")
    public void successfulPayment(@Valid @RequestBody UUID paymentId) {
        log.info("Эмуляции успешной оплаты  paymentID {}", paymentId);
        paymentService.successfulPayment(paymentId);
    }

    @PostMapping("/productCost")
    public Double calculateProductCostPayment(@Valid @RequestBody OrderDto orderDto) {
        log.info("Производим расчет стоимости товаров в заказе с ID = {}", orderDto.getOrderId());
        return paymentService.calculateProductCostPayment(orderDto);
    }

    @PostMapping("/failed")
    public void failedPayment(@RequestBody UUID paymentId) {
        log.info("Эмуляция отказа в оплате paymentId {}.", paymentId);
        paymentService.failedPayment(paymentId);
    }
}