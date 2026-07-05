package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.client.warehouse.WarehouseClientFallback;

import java.util.Map;

@FeignClient(
        name = "warehouse",
        fallback = WarehouseClientFallback.class
)
public interface WarehouseClient {

    @PostMapping("/api/v1/warehouse/reserve")
    Boolean checkAndReserveItems(@RequestBody Map<Long, Integer> items);
}