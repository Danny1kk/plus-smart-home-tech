package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public List<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping("/product")
    public ProductDto addProduct(@RequestBody ProductDto productDto) {
        return productService.addProduct(productDto);
    }

    @GetMapping("/quantityState")
    public void checkQuantityState(@RequestParam Long productId, @RequestParam String quantityState) {
        productService.checkQuantityState(productId, quantityState);
    }
}