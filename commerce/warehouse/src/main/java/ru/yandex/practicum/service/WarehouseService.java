package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WarehouseService {

    private final Map<Long, Integer> stock = new ConcurrentHashMap<>();

    public synchronized Boolean reserveItems(Map<Long, Integer> items) {
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            int available = stock.getOrDefault(entry.getKey(), 0);
            if (available < entry.getValue()) {
                return false;
            }
        }

        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            int available = stock.get(entry.getKey());
            stock.put(entry.getKey(), available - entry.getValue());
        }

        return true;
    }

    public void addStock(Long productId, Integer quantity) {
        stock.put(productId, stock.getOrDefault(productId, 0) + quantity);
    }
}