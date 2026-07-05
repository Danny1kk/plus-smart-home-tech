package ru.yandex.practicum.client.warehouse;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.warehouse.AddToCartRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedDto;
import ru.yandex.practicum.dto.warehouse.WarehouseRequest;

@FeignClient(
        name = "warehouse",
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
}