package ru.yandex.practicum.client.payment;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.util.UUID;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentFeignClient {
    PaymentDto makingPaymentForOrder(@Valid @RequestBody OrderDto orderDto) throws FeignException;

    Double calculateTotalCostPayment(@Valid @RequestBody OrderDto orderDto) throws FeignException;

    void successfulPayment(@Valid @RequestBody UUID paymentId) throws FeignException;

    Double calculateProductCostPayment(@Valid @RequestBody OrderDto orderDto) throws FeignException;

    void failedPayment(@RequestBody UUID paymentId) throws FeignException;

}