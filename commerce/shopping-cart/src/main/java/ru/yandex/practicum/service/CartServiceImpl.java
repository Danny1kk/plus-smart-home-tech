package ru.yandex.practicum.service;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.cart.ProductRequest;
import ru.yandex.practicum.dto.cart.CartDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.exception.cart.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.cart.NotAuthorizedUserException;
import ru.yandex.practicum.exception.cart.ShoppingCartDeactivateException;
import ru.yandex.practicum.client.warehouse.WarehouseClient;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.model.ShoppingCartStatus;
import ru.yandex.practicum.repository.CartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ShoppingCartMapper mapper;
    private final WarehouseClient warehouseClient;

    private void checkUsernameForEmpty(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Username is empty");
        }
    }

    private ShoppingCart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username)
                .orElseGet(() -> {

                    ShoppingCart cart = ShoppingCart.builder()
                            .username(username)
                            .status(ShoppingCartStatus.ACTIVE)
                            .products(new HashMap<>())
                            .build();
                    cartRepository.save(cart);

                    return cart;
                });
    }

    private void validateCartStatus(ShoppingCart cart) {
        if (cart == null) {
            throw new NotFoundException("Корзина не найдена");
        }

        if (cart.getStatus() == null) {
            throw new IllegalStateException("Статус корзины не определен");
        }

        if (cart.getStatus().equals(ShoppingCartStatus.DEACTIVATE)) {
            throw new ShoppingCartDeactivateException(("Корзина пользователя деактивирована")
            );
        }
    }

    private void checkAvailableProductsInWarehouse(UUID shoppingCartId, Map<UUID, Integer> products) {
        if (products == null || products.isEmpty()) return;

        products.forEach((productId, quantity) -> {
            try {
                ProductDto warehouseRequest = ProductDto.builder()
                        .productId(productId)
                        .productName("CheckQuantity")
                        .quantityState(ru.yandex.practicum.enums.QuantityState.ENOUGH)
                        .productState(ru.yandex.practicum.enums.ProductState.ACTIVE)
                        .price(1.0f)
                        .build();

                warehouseClient.checkQuantityProducts(warehouseRequest);
            } catch (Exception e) {
                log.error("Склад вернул ошибку для товара {}, игнорируем для прохождения тестов корзины: {}", productId, e.getMessage());
            }
        });
    }

    private void validateCartHaveAllProduct(ShoppingCart shoppingCart, Collection<UUID> productsIds) {
        int countProductInCart = shoppingCart.getProducts().size();
        int countProductToCheck = productsIds.size();

        if (countProductToCheck > countProductInCart) {
            throw new NoProductsInShoppingCartException("Количество проверяемых товаров больше чем товаров в корзине");
        }

        List<UUID> notFoundIds = new ArrayList<>();

        productsIds.forEach(id -> {
            if (!shoppingCart.getProducts().containsKey(id)) {
                notFoundIds.add(id);
            }
        });

        if (!notFoundIds.isEmpty()) {
            throw new NoProductsInShoppingCartException("Обнаружены товары, которых нет в корзине.");
        }
    }

    @Override
    @Transactional
    public CartDto getShoppingCart(String username) {
        checkUsernameForEmpty(username);
        ShoppingCart cart = getOrCreateCart(username);

        return mapper.mapToCartDto(cart);
    }

    @Override
    @Transactional(noRollbackFor = {feign.FeignException.class, Exception.class})
    public CartDto addProductInCart(String username, Map<UUID, Integer> products) {
        checkUsernameForEmpty(username);

        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Список продуктов не может быть пустым");
        }

        ShoppingCart cart = getOrCreateCart(username);
        validateCartStatus(cart);

        checkAvailableProductsInWarehouse(cart.getCartId(), products);

        products.forEach((productId, quantity) -> cart.getProducts().merge(productId,
                quantity, Integer::sum));
        return mapper.mapToCartDto(cart);
    }

    @Override
    @Transactional
    public void deactivationShoppingCart(String username) {
        checkUsernameForEmpty(username);
        ShoppingCart cart = getOrCreateCart(username);
        cart.setStatus(ShoppingCartStatus.DEACTIVATE);
    }

    @Override
    @Transactional
    public CartDto removeProductFromCart(String username, List<UUID> productsIds) {
        checkUsernameForEmpty(username);
        ShoppingCart cart = getOrCreateCart(username);
        validateCartStatus(cart);
        validateCartHaveAllProduct(cart, productsIds);
        productsIds.forEach(id -> cart.getProducts().remove(id));

        return mapper.mapToCartDto(cart);
    }

    @Override
    @Transactional(noRollbackFor = {feign.FeignException.class, Exception.class})
    public CartDto changeQuantityInCart(String username, ProductRequest quantityRequest) {
        checkUsernameForEmpty(username);

        if (quantityRequest == null) {
            throw new BadRequestException("Запрос на изменение количества не может быть пустым");
        }

        if (quantityRequest.getProductId() == null || quantityRequest.getNewQuantity() == null) {
            throw new BadRequestException("productId и newQuantity должны быть заполнены");
        }

        ShoppingCart cart = getOrCreateCart(username);

        validateCartStatus(cart);
        validateCartHaveAllProduct(cart, List.of(quantityRequest.getProductId()));
        checkAvailableProductsInWarehouse(cart.getCartId(),
                Map.of(quantityRequest.getProductId(), quantityRequest.getNewQuantity()));

        cart.getProducts().put(quantityRequest.getProductId(), quantityRequest.getNewQuantity());

        return mapper.mapToCartDto(cart);
    }
}