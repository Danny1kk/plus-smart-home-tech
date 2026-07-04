package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.service.CartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartDto getCart(@RequestParam(value = "username") String queryUsername,
                           @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = resolveUserId(queryUsername, headerUserId);
        return cartService.getCart(resolvedUid);
    }

    @PostMapping("/change-quantity")
    public CartDto changeQuantity(@RequestParam("username") String username,
                                  @RequestBody ChangeProductQuantityRequest request) {

        return cartService.changeQuantity(
                username,
                request.getProductId(),
                request.getNewQuantity()
        );
    }

    @PostMapping("/remove")
    public CartDto removeProduct(@RequestParam("username") String username,
                                 @RequestBody List<UUID> productIds) {

        return cartService.removeProducts(username, productIds);
    }

    @PutMapping
    public CartDto addProduct(
            @RequestParam("username") String username,
            @RequestBody Map<UUID, Long> products) {

        return cartService.addProducts(username, products);
    }

    @DeleteMapping
    public void deactivate(
            @RequestParam("username") String username) {

        cartService.deactivate(username);
    }

    private String resolveUserId(String queryUsername, String headerUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return headerUserId;
        }
        return queryUsername;
    }
}