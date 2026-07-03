package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> checkAndReserveItems(@RequestBody Map<Long, Integer> itemsToReserve) {
        if (itemsToReserve == null || itemsToReserve.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }

        try {
            boolean success = warehouseService.reserveItems(itemsToReserve);

            if (success) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }

    @PostMapping(path = {"", "/product", "/goods"})
    public ProductDto createProductDto(@RequestBody ProductDto request) {
        if (request.getId() != null) {
            warehouseService.addStock(request.getId(), 0);
        }
        return request;
    }

    @PutMapping(path = {"", "/storage", "/goods"})
    public ProductDto addStockPut(@RequestParam(required = false) Long productId,
                                  @RequestParam(required = false) Integer quantity,
                                  @RequestBody(required = false) ProductDto requestBody) {
        if (productId != null && quantity != null) {
            warehouseService.addStock(productId, quantity);
        } else if (requestBody != null && requestBody.getId() != null) {
            int qty = 1;

            if (requestBody.getQuantity() != null) {
                try {
                    qty = Integer.parseInt(String.valueOf(requestBody.getQuantity()));
                } catch (NumberFormatException e) {
                    qty = 1;
                }
            }

            warehouseService.addStock(requestBody.getId(), qty);
            return requestBody;
        }

        ProductDto response = (requestBody != null) ? requestBody : new ProductDto();
        if (productId != null) response.setId(productId);

        if (quantity != null) response.setQuantity(quantity);

        return response;
    }

    @PostMapping(path = {"/add", "/storage", "/goods/receipt"})
    public ProductDto addStockPost(@RequestParam(required = false) Long productId,
                                   @RequestParam(required = false) Integer quantity,
                                   @RequestBody(required = false) ProductDto requestBody) {
        ProductDto response = new ProductDto();

        if (productId != null && quantity != null) {
            warehouseService.addStock(productId, quantity);
            response.setId(productId);
            response.setQuantity(quantity);
        } else if (requestBody != null && requestBody.getId() != null) {
            int qty = (requestBody.getQuantity() != null) ? requestBody.getQuantity() : 1;
            warehouseService.addStock(requestBody.getId(), qty);
            return requestBody;
        }

        return response;
    }

    @PostMapping("/goods/receipt/dto")
    public StockReceiptDto addStockReceipt(@RequestBody StockReceiptDto dto) {
        if (dto != null) {
            warehouseService.addStock(dto.getProductId(), dto.getQuantity());
        }
        return dto;
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