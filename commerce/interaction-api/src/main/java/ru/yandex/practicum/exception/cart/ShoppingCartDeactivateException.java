package ru.yandex.practicum.exception.cart;

public class ShoppingCartDeactivateException extends RuntimeException {
    public ShoppingCartDeactivateException(String message) {
        super(message);
    }
}