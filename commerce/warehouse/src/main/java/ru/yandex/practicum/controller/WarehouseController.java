package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/reserve")
    public Boolean checkAndReserveItems(@RequestBody Map<Long, Integer> items) {
        return warehouseService.reserveItems(items);
    }

    @PostMapping("/items")
    public void addStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        warehouseService.addStock(productId, quantity);
    }
}