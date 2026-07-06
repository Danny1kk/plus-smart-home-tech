package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookedDto {
    @NotNull
    @Positive
    Double deliveryWeight;

    @NotNull
    @Positive
    Double deliveryVolume;

    @NotNull
    Boolean fragile;
}
