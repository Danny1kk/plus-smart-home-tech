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
    public void addStockReceipt(@RequestParam(required = false) Long productId,
                                @RequestParam(required = false) Integer quantity,
                                @RequestBody(required = false) Map<String, Object> body) {
        if (productId != null && quantity != null) {
            warehouseService.addStock(productId, quantity);
        } else if (body != null) {
            Long id = Long.valueOf(body.get("productId").toString());
            Integer qty = Integer.valueOf(body.get("quantity").toString());
            warehouseService.addStock(id, qty);
        }
    }

    @PutMapping("/goods")
    public void addStockGoodsPut(@RequestParam(required = false) Long productId,
                                 @RequestParam(required = false) Integer quantity,
                                 @RequestBody(required = false) Map<String, Object> body) {
        if (productId != null && quantity != null) {
            warehouseService.addStock(productId, quantity);
        } else if (body != null) {
            Long id = Long.valueOf(body.get("productId").toString());
            Integer qty = Integer.valueOf(body.get("quantity").toString());
            warehouseService.addStock(id, qty);
        }
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