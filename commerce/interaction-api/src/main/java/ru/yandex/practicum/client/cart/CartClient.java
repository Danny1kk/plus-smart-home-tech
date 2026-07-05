package ru.yandex.practicum.client.cart;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.cart.ProductRequest;
import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.dto.store.ProductPageDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface CartClient {
    @GetMapping
    ProductPageDto getShoppingCart(@RequestParam String userName) throws FeignException;

    @PutMapping
    CartDto addProductInCart(@RequestParam String userName,
                             @RequestBody @NotEmpty Map<UUID, @NotNull @Positive Integer> products) throws FeignException;

    @DeleteMapping
    void deactivationShoppingCart(@RequestParam String userName) throws FeignException;

    @PostMapping("/remove")
    CartDto removeProductFromCart(@RequestParam String userName,
                                  @RequestBody @NotEmpty List<UUID> productsIds) throws FeignException;

    @PostMapping("/change-quantity")
    CartDto changeQuantityInCart(@RequestParam String userName,
                                 @Valid @RequestBody ProductRequest quantityRequest) throws FeignException;
}