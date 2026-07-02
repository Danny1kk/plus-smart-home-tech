package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.StockReceiptDto;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;

@RestController
@RequestMapping(path = {
        "/api/v1/warehouse",
        "/api/v1/goods",
        "/goods",
        "/api/v1/storage"
})
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/reserve")
    public Boolean checkAndReserveItems(@RequestBody Map<Long, Integer> items) {
        return warehouseService.reserveItems(items);
    }

    @PostMapping(path = {"/product", "/goods"})
    public void createProductDto(@RequestBody ProductDto request) {
        if (request.getId() != null) {
            warehouseService.addStock(request.getId(), 0);
        }
    }

    @PostMapping(path = {"/storage", "/goods/receipt"})
    public void addStockPost(@RequestParam(required = false) Long productId,
                             @RequestParam(required = false) Integer quantity,
                             @RequestBody(required = false) ProductDto requestBody) {
        if (productId != null && quantity != null) {
            warehouseService.addStock(productId, quantity);
        } else if (requestBody != null && requestBody.getId() != null) {
            warehouseService.addStock(requestBody.getId(), 1);
        }
    }

    @PutMapping(path = {"/storage", "/goods"})
    public void addStockPut(@RequestParam(required = false) Long productId,
                            @RequestParam(required = false) Integer quantity,
                            @RequestBody(required = false) ProductDto requestBody) {
        if (productId != null && quantity != null) {
            warehouseService.addStock(productId, quantity);
        } else if (requestBody != null && requestBody.getId() != null) {
            warehouseService.addStock(requestBody.getId(), 1);
        }
    }

    @PostMapping("/goods/receipt")
    public void addStockReceipt(@RequestBody StockReceiptDto dto) {
        warehouseService.addStock(dto.getProductId(), dto.getQuantity());
    }

    @PutMapping("/goods")
    public void addStockGoodsPut(@RequestBody StockReceiptDto dto) {
        warehouseService.addStock(dto.getProductId(), dto.getQuantity());
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