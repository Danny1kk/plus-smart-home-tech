package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddToCartRequest;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.service.CartService;

import java.util.Map;

@RestController
@RequestMapping(path = {"/api/v1/cart", "/api/v1/shopping-cart"})
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private String resolveUserId(String queryUsername, String pathUserId, String headerUserId) {
        if (queryUsername != null && !queryUsername.trim().isEmpty()) return queryUsername;
        if (pathUserId != null && !pathUserId.trim().isEmpty()) return pathUserId;
        return (headerUserId != null && !headerUserId.trim().isEmpty()) ? headerUserId : "default_user";
    }

    @GetMapping(path = {"", "/{userId}"})
    public CartDto getCart(@RequestParam(value = "username", required = false) String queryUsername,
                           @PathVariable(required = false) String userId,
                           @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
        return cartService.getCart(resolvedUid);
    }

    @PutMapping(path = {"", "/{userId}"})
    public CartDto updateCart(@RequestParam(value = "username", required = false) String queryUsername,
                              @PathVariable(required = false) String userId,
                              @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId,
                              @RequestBody Map<String, Integer> items) {
        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
        cartService.clearCart(resolvedUid);
        if (items != null) {
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                cartService.addItem(resolvedUid, entry.getKey(), entry.getValue());
            }
        }
        return cartService.getCart(resolvedUid);
    }

    @PostMapping(path = {"/add", "/{userId}/add"})
    public CartDto addItem(@RequestParam(value = "username", required = false) String queryUsername,
                           @PathVariable(required = false) String userId,
                           @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId,
                           @RequestBody AddToCartRequest request) {
        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
        return cartService.addItem(resolvedUid, request.getProductId(), request.getQuantity());
    }

    @DeleteMapping(path = {"", "/{userId}"})
    public CartDto clearCart(@RequestParam(value = "username", required = false) String queryUsername,
                             @PathVariable(required = false) String userId,
                             @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
        cartService.clearCart(resolvedUid);
        return cartService.getCart(resolvedUid);
    }

    @PostMapping("/remove")
    public CartDto removeProduct(@RequestParam(value = "username", required = false) String queryUsername,
                                 @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId,
                                 @RequestBody(required = false) Map<String, Integer> items) {
        String resolvedUid = resolveUserId(queryUsername, null, headerUserId);
        if (items != null) {
            for (String productId : items.keySet()) {
                cartService.removeItem(resolvedUid, productId);
            }
        }
        return cartService.getCart(resolvedUid);
    }

    @PostMapping("/change-quantity")
    public CartDto changeQuantity(@RequestParam(value = "username", required = false) String queryUsername,
                                  @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId,
                                  @RequestBody AddToCartRequest request) {
        String resolvedUid = resolveUserId(queryUsername, null, headerUserId);
        return cartService.changeQuantity(resolvedUid, request.getProductId(), request.getQuantity());
    }

    @DeleteMapping(path = {"/deactivate", "/{userId}/deactivate"})
    public CartDto deactivateCartDelete(@PathVariable(required = false) String userId,
                                        @RequestParam(value = "username", required = false) String queryUsername,
                                        @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
        cartService.clearCart(resolvedUid);
        return cartService.getCart(resolvedUid);
    }

    @PostMapping(path = {"/deactivate", "/{userId}/deactivate"})
    public CartDto deactivateCartPost(@PathVariable(required = false) String userId,
                                      @RequestParam(value = "username", required = false) String queryUsername,
                                      @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        return deactivateCartDelete(userId, queryUsername, headerUserId);
    }
}