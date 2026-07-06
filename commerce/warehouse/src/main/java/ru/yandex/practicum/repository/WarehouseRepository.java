package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.WarehouseProduct;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseProduct, UUID> {
    default Map<UUID, WarehouseProduct> findAllAsMapByIds(Iterable<UUID> ids) {
        return findAllById(ids).stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, product -> product));
    }
}