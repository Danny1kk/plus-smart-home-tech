package ru.yandex.practicum.exception.warehouse;

import java.util.List;
import java.util.UUID;

public class MultipleProductsNotFoundException extends RuntimeException {
    private final List<UUID> missingProductIds;

    public MultipleProductsNotFoundException(String message, List<UUID> missingProductIds) {
        super(message);
        this.missingProductIds = missingProductIds;
    }

    public List<UUID> getMissingProductIds() {
        return missingProductIds;
    }
}