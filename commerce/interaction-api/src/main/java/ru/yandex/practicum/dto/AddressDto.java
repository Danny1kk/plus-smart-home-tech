package ru.yandex.practicum.dto;

import lombok.Data;

@Data
public class AddressDto {
    private String country;
    private String city;
    private String street;
    private String house;
    private String structure;

    public AddressDto(String russia, String moscow, String lenina, String number, String number1) {
    }
}