package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.dto.warehouse.DimensionDto;
import ru.yandex.practicum.model.Dimension;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DimensionMapper {
    Dimension mapToDimension(DimensionDto dimensionDto);
}