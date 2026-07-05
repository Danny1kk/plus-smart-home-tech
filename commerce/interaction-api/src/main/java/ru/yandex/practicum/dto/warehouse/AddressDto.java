package ru.yandex.practicum.dto.warehouse;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressDto {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}