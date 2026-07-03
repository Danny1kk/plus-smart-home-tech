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

    @GetMapping
    public CartDto getCartByHeader(@RequestHeader("X-Main-Academy-Smart-Home-User-Id") String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/add")
    public CartDto addItem(@PathVariable String userId, @RequestBody AddToCartRequest request) {
        return cartService.addItem(userId, request.getProductId(), request.getQuantity());
    }

    @PostMapping("/add")
    public CartDto addItemByHeader(@RequestHeader("X-Main-Academy-Smart-Home-User-Id") String userId,
                                   @RequestBody AddToCartRequest request) {
        return cartService.addItem(userId, request.getProductId(), request.getQuantity());
    }

    @DeleteMapping("/{userId}")
    public CartDto clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return cartService.getCart(userId);
    }

    @DeleteMapping
    public CartDto clearCartByHeader(@RequestHeader("X-Main-Academy-Smart-Home-User-Id") String userId) {
        cartService.clearCart(userId);
        return cartService.getCart(userId);
    }

    @DeleteMapping("/remove")
    public CartDto removeProduct(@RequestHeader("X-Main-Academy-Smart-Home-User-Id") String userId,
                                 @RequestBody AddToCartRequest request) {
        return cartService.removeItem(userId, request.getProductId());
    }

    @PostMapping("/change-quantity")
    public CartDto changeQuantity(@RequestHeader("X-Main-Academy-Smart-Home-User-Id") String userId,
                                  @RequestBody AddToCartRequest request) {
        return cartService.changeQuantity(userId, request.getProductId(), request.getQuantity());
    }

    @DeleteMapping(path = {"/deactivate", "/{userId}/deactivate"})
    public CartDto deactivateCartDelete(@PathVariable(required = false) String userId,
                                        @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = userId != null ? userId : headerUserId;
        if (resolvedUid != null) {
            cartService.clearCart(resolvedUid);
            return cartService.getCart(resolvedUid);
        }
        return new CartDto();
    }

    @PostMapping(path = {"/deactivate", "/{userId}/deactivate"})
    public CartDto deactivateCartPost(@PathVariable(required = false) String userId,
                                      @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = userId != null ? userId : headerUserId;
        if (resolvedUid != null) {
            cartService.clearCart(resolvedUid);
            return cartService.getCart(resolvedUid);
        }
        return new CartDto();
    }

    @PutMapping
    public CartDto deactivateCartPut(@RequestParam("username") String userId) {
        cartService.clearCart(userId);
        return cartService.getCart(userId);
    }
}