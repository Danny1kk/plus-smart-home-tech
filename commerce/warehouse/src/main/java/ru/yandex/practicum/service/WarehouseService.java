package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WarehouseService {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    public synchronized Boolean reserveItems(Map<String, Integer> items) {
        if (items == null) return false;

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            int available = stock.getOrDefault(entry.getKey(), 0);
            if (available < entry.getValue()) {
                return false;
            }
        }

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            int available = stock.getOrDefault(entry.getKey(), 0);
            stock.put(entry.getKey(), available - entry.getValue());
        }

        return true;
    }

    public void addStock(String productId, Integer quantity) {
        if (productId != null && quantity != null) {
            stock.put(productId, stock.getOrDefault(productId, 0) + quantity);
        }
    }
}