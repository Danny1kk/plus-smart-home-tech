package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddToCartRequest;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.service.CartService;

@RestController
@RequestMapping(path = {"/api/v1/cart", "/api/v1/shopping-cart"})
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public CartDto getCart(@PathVariable String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/add")
    public CartDto addItem(@PathVariable String userId, @RequestBody AddToCartRequest request) {
        return cartService.addItem(userId, request.getProductId(), request.getQuantity());
    }

    @DeleteMapping("/{userId}")
    public CartDto clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return cartService.getCart(userId);
    }

    @DeleteMapping(path = {"", "/deactivate", "/{userId}/deactivate"})
    public CartDto deactivateCartDelete(@PathVariable(required = false) String userId) {
        if (userId != null) {
            cartService.clearCart(userId);
            return cartService.getCart(userId);
        }
        return new CartDto();
    }

        @PostMapping(path = {"", "/deactivate", "/{userId}/deactivate"})
        public CartDto deactivateCartPost(@PathVariable(required = false) String userId) {
            if (userId != null) {
                cartService.clearCart(userId);
                return cartService.getCart(userId);
            }
            return new CartDto();
    }
}