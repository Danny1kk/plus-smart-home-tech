package ru.yandex.practicum.client.warehouse;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.warehouse.AddToCartRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedDto;
import ru.yandex.practicum.dto.warehouse.WarehouseRequest;

@Component
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public void newProduct(WarehouseRequest newRequest) {
        throw new WarehouseFallbackException("Fallback response: сервис WAREHOUSE временно недоступен");
    }

    @Override
    public BookedDto checkQuantityProducts(ProductDto productDto) {
        throw new WarehouseFallbackException("Fallback response: сервис WAREHOUSE временно недоступен");
    }

    @Override
    public void addQuantityProduct(AddToCartRequest addRequest) {
        throw new WarehouseFallbackException("Fallback response: сервис WAREHOUSE временно недоступен");
    }

    @Override
    public AddressDto getAddress() {
        throw new WarehouseFallbackException("Fallback response: сервис WAREHOUSE временно недоступен");
    }
}