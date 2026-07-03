package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
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
    public List<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping(path = {"", "/product"})
    public ProductDto addProductPost(@RequestBody ProductDto productDto) {
        return productService.addProduct(productDto);
    }

    @PutMapping(path = {"", "/product"})
    public ProductDto addProductPut(@RequestBody ProductDto productDto) {
        return productService.addProduct(productDto);
    }

    @GetMapping("/quantityState")
    public void checkQuantityState(@RequestParam Long productId, @RequestParam String quantityState) {
        productService.checkQuantityState(productId, quantityState);
    }

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable String id) {
        if (id == null || "null".equals(id) || "undefined".equals(id)) {
            return new ProductDto();
        }
        try {
            Long numericId = Long.parseLong(id);
            return productService.getProductById(numericId);
        } catch (NumberFormatException e) {
            return new ProductDto();
        }
    }
}