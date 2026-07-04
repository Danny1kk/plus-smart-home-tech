package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddToCartRequest;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartDto getCart(@RequestParam(value = "username", required = false) String queryUsername,
                           @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = resolveUserId(queryUsername, headerUserId);
        return cartService.getCart(resolvedUid);
    }

    @PostMapping("/change-quantity")
    public CartDto changeQuantity(@RequestParam(value = "username", required = false) String queryUsername,
                                  @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId,
                                  @RequestBody AddToCartRequest request) {
        String resolvedUid = resolveUserId(queryUsername, headerUserId);
        return cartService.changeQuantity(resolvedUid, request.getProductId(), request.getQuantity());
    }

    @PostMapping("/remove")
    public CartDto removeProduct(@RequestParam(value = "username", required = false) String queryUsername,
                                 @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId,
                                 @RequestBody(required = false) List<String> productIds) {
        String resolvedUid = resolveUserId(queryUsername, headerUserId);
        if (productIds != null) {
            for (String productId : productIds) {
                cartService.removeItem(resolvedUid, productId);
            }
        }
        return cartService.getCart(resolvedUid);
    }

    private String resolveUserId(String queryUsername, String headerUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return headerUserId;
        }
        return queryUsername;
    }
}