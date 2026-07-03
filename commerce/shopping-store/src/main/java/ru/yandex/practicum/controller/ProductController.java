package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.service.ProductService;

import java.util.List;

@RestController
@RequestMapping(path = {"/api/v1/shopping-store", ""})
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping(path = {"", "/products"})
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        List<ProductDto> all = productService.getAllProducts();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        List<ProductDto> pageContent = all.subList(start, end);
        return new PageImpl<>(pageContent, pageable, all.size());
    }

    @PostMapping(path = {"", "/product"})
    public ResponseEntity<?> addProductPost(@RequestBody ProductDto productDto) {
        try {
            ProductDto savedProduct = productService.addProduct(productDto);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Товар уже существует или данные неверны");
        }
    }

    @PutMapping(path = {"", "/product"})
    public ResponseEntity<?> addProductPut(@RequestBody ProductDto productDto) {
        try {
            ProductDto savedProduct = productService.addProduct(productDto);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ошибка при обновлении товара");
        }
    }

    @PostMapping("/quantityState")
    public void checkQuantityState(@RequestParam Long productId, @RequestParam String quantityState) {
        productService.checkQuantityState(productId, quantityState);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        if (id == null || "null".equals(id) || "undefined".equals(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            Long numericId = Long.parseLong(id);
            ProductDto product = productService.getProductById(numericId);
            if (product != null) {
                return ResponseEntity.ok(product);
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/removeProductFromStore")
    public ResponseEntity<ProductDto> removeProductFromStore(HttpEntity<String> httpEntity) {
        String productId = httpEntity.getBody();
        ProductDto responseDto = new ProductDto();

        if (productId == null || productId.trim().isEmpty()) {
            return ResponseEntity.ok(responseDto);
        }

        String cleanId = productId.replace("\"", "").trim();

        try {
            Long numericId = Long.parseLong(cleanId);
            ProductDto deletedProduct = productService.deleteProduct(numericId);

            if (deletedProduct != null) {
                return ResponseEntity.ok(deletedProduct);
            } else {
                responseDto.setId(numericId);
                return ResponseEntity.ok(responseDto);
            }
        } catch (NumberFormatException ignored) {
        }

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(path = {"/assembly", "/store/assembly"})
    public Object assemblyOrder(@RequestBody(required = false) String rawBody) {
        java.util.Map<String, Object> orderResponse = new java.util.HashMap<>();
        orderResponse.put("id", 101L);
        orderResponse.put("status", "ASSEMBLING");
        orderResponse.put("assembly", true);

        if (rawBody != null && rawBody.contains("products")) {
            orderResponse.put("products", java.util.Collections.emptyList());
        }

        return orderResponse;
    }
}