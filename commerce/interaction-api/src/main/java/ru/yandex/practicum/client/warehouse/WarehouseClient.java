package ru.yandex.practicum.client.warehouse;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(
        name = "warehouse",
        path = "/api/v1/warehouse",
        fallback = WarehouseClientFallback.class
)
public interface WarehouseClient {
    @PutMapping
    void newProduct(@Valid @RequestBody WarehouseRequest newRequest) throws FeignException;

    @PostMapping("/check")
    BookedDto checkQuantityProducts(@Valid @RequestBody ProductDto productDto) throws FeignException;

    @PostMapping("/add")
    void addQuantityProduct(@Valid @RequestBody AddToCartRequest addRequest) throws FeignException;

    @GetMapping("/address")
    AddressDto getAddress() throws FeignException;

    @PostMapping("/shipped")
    void shippedProductForDelivery(@Valid @RequestBody DeliveryRequest shippedRequest) throws FeignException;

    @PostMapping("/return")
    void returnProductToTheWarehouse(@RequestBody Map<UUID, @NotNull @Positive Integer> products) throws FeignException;

    @PostMapping("/assembly")
    BookedDto assemblyProductOrderDelivery(
            @Valid @RequestBody ProductsOrderRequest assemblyRequest) throws FeignException;
}