package ru.yandex.practicum.client;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public Boolean checkAndReserveItems(Map<Long, Integer> items) {
        throw new IllegalStateException("Сервис склада временно недоступен. Мы не смогли проверить наличие товаров, попробуйте позже.");
    }
}