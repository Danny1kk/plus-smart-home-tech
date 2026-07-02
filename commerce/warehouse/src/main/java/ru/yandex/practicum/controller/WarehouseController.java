package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.ProductDto;
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

    @PostMapping("/product")
    public void createProductDto(@RequestBody ProductDto request) {
        if (request.getId() != null) {
            warehouseService.addStock(request.getId(), 0);
        }
    }

    @PostMapping("/storage")
    public void addStockPost(@RequestParam Long productId, @RequestParam Integer quantity) {
        warehouseService.addStock(productId, quantity);
    }

    @PutMapping("/storage")
    public void addStockPut(@RequestParam Long productId, @RequestParam Integer quantity) {
        warehouseService.addStock(productId, quantity);
    }

    @PostMapping("/goods/receipt")
    public void addStockReceipt(@RequestParam Long productId, @RequestParam Integer quantity) {
        warehouseService.addStock(productId, quantity);
    }

    @PutMapping("/goods")
    public void addStockGoodsPut(@RequestParam Long productId, @RequestParam Integer quantity) {
        warehouseService.addStock(productId, quantity);
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        AddressDto address = new AddressDto();
        address.setCountry("Russia");
        address.setCity("Moscow");
        address.setStreet("Lenina");
        address.setHouse("10");
        address.setStructure("1");
        address.setFlat("42");
        return address;
    }
}