package ru.yandex.practicum.client.warehouse;

public class WarehouseFallbackException extends RuntimeException {
    public WarehouseFallbackException(String message) {
        super(message);
    }
}