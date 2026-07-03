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

    private String resolveUserId(String queryUsername, String pathUserId, String headerUserId) {
        if (queryUsername != null && !queryUsername.trim().isEmpty()) return queryUsername;
        if (pathUserId != null && !pathUserId.trim().isEmpty()) return pathUserId;
        return headerUserId;
    }

    @GetMapping(path = {"", "/{userId}"})
    public CartDto getCart(@RequestParam(value = "username", required = false) String queryUsername,
                           @PathVariable(required = false) String userId,
                           @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
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
    public Object clearCart(@RequestParam(value = "username", required = false) String queryUsername,
                            @PathVariable(required = false) String userId,
                            @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {

        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
        if (resolvedUid == null || resolvedUid.trim().isEmpty()) {
            resolvedUid = "default_user";
        }

        try {
            cartService.clearCart(resolvedUid);
        } catch (Exception e) {
            System.out.println("Ошибка при очистке корзины: " + e.getMessage());
        }

        try {
            CartDto cartDto = cartService.getCart(resolvedUid);
            if (cartDto != null) {
                java.util.Map<Long, Integer> standardMap = cartDto.getItems() != null ?
                        new java.util.HashMap<>(cartDto.getItems()) : new java.util.HashMap<>();
                return new CartDto(cartDto.getUserId(), standardMap);
            }
        } catch (Exception e) {
            System.out.println("Ошибка сериализации при очистке: " + e.getMessage());
        }

        return new CartDto(resolvedUid, new java.util.HashMap<>());
    }

    @PostMapping("/remove")
    public Object removeProduct(@RequestParam(value = "username", required = false) String queryUsername,
                                @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId,
                                @RequestBody(required = false) String rawBody) {

        String resolvedUid = resolveUserId(queryUsername, null, headerUserId);
        if (resolvedUid == null || resolvedUid.trim().isEmpty()) {
            resolvedUid = "default_user";
        }

        try {
            if (rawBody != null && !rawBody.trim().isEmpty()) {
                String body = rawBody.trim();

                if (body.startsWith("[")) {
                    String clean = body.replace("[", "").replace("]", "").replace("\"", "");
                    String[] ids = clean.split(",");
                    for (String id : ids) {
                        if (!id.trim().isEmpty()) {
                            try {
                                cartService.removeItem(resolvedUid, Long.parseLong(id.trim()));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                else if (body.startsWith("{")) {
                    if (body.contains("productId")) {
                        String idStr = body.replaceAll("(?s).*\"productId\"\\s*:\\s*\"?([0-9a-zA-Z\\-]+)\"?.*", "$1");
                        if (!idStr.equals(body) && !idStr.isEmpty()) {
                            try {
                                Long productId = Long.parseLong(idStr.replaceAll("[^0-9]", ""));
                                cartService.removeItem(resolvedUid, productId);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                else {
                    try {
                        Long productId = Long.parseLong(body.replace("\"", "").trim());
                        cartService.removeItem(resolvedUid, productId);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка при разборе тела удаления: " + e.getMessage());
        }

        try {
            CartDto cartDto = cartService.getCart(resolvedUid);

            if (cartDto != null && cartDto.getItems() != null) {
                java.util.Map<Long, Integer> standardMap = new java.util.HashMap<>(cartDto.getItems());
                return new CartDto(cartDto.getUserId(), standardMap);
            }
            return cartDto;
        } catch (Exception e) {
            System.out.println("Критическая ошибка при формировании ответа корзины: " + e.getMessage());
            return new CartDto(resolvedUid, new java.util.HashMap<>());
        }
    }

    @PostMapping("/change-quantity")
    public CartDto changeQuantity(@RequestParam(value = "username", required = false) String queryUsername,
                                  @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId,
                                  @RequestBody AddToCartRequest request) {

        String resolvedUid = resolveUserId(queryUsername, null, headerUserId);
        if (resolvedUid == null || resolvedUid.trim().isEmpty()) {
            resolvedUid = "default_user";
        }

        return cartService.changeQuantity(resolvedUid, request.getProductId(), request.getQuantity());
    }

    @DeleteMapping(path = {"/deactivate", "/{userId}/deactivate"})
    public Object deactivateCartDelete(@PathVariable(required = false) String userId,
                                       @RequestParam(value = "username", required = false) String queryUsername,
                                       @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
        if (resolvedUid == null || resolvedUid.trim().isEmpty()) {
            resolvedUid = "default_user";
        }

        try {
            cartService.clearCart(resolvedUid);
            CartDto cartDto = cartService.getCart(resolvedUid);
            if (cartDto != null) {
                return new CartDto(cartDto.getUserId(), new java.util.HashMap<>(cartDto.getItems()));
            }
        } catch (Exception ignored) {}

        return new CartDto(resolvedUid, new java.util.HashMap<>());
    }

    @PostMapping(path = {"/deactivate", "/{userId}/deactivate"})
    public Object deactivateCartPost(@PathVariable(required = false) String userId,
                                     @RequestParam(value = "username", required = false) String queryUsername,
                                     @RequestHeader(value = "X-Main-Academy-Smart-Home-User-Id", required = false) String headerUserId) {
        String resolvedUid = resolveUserId(queryUsername, userId, headerUserId);
        if (resolvedUid == null || resolvedUid.trim().isEmpty()) {
            resolvedUid = "default_user";
        }

        try {
            cartService.clearCart(resolvedUid);
            CartDto cartDto = cartService.getCart(resolvedUid);
            if (cartDto != null) {
                return new CartDto(cartDto.getUserId(), new java.util.HashMap<>(cartDto.getItems()));
            }
        } catch (Exception ignored) {}

        return new CartDto(resolvedUid, new java.util.HashMap<>());
    }
}