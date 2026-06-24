//package ru.yandex.practicum.model;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import lombok.AccessLevel;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.experimental.FieldDefaults;
//import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
//
//
//@Entity
//@Table(name = "actions")
//@Getter
//@Setter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class Action {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    Long id;
//
//    @Column(name = "type")
//    @Enumerated(EnumType.STRING)
//    ActionTypeAvro type;
//
//    @Column(name = "value")
//    Integer value;
//
//}

package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

@Entity
@Table(name = "actions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    // Решение 1: явно указываем тип VARCHAR(255) для enum,
    // чтобы Hibernate не пытался создать в H2 неподдерживаемый тип ENUM(...)
    @Column(name = "type", columnDefinition = "VARCHAR(255)")
    @Enumerated(EnumType.STRING)
    ActionTypeAvro type;

    // Решение 2: Переименовываем колонку из зарезервированного слова "value" в "action_value".
    // Само поле в Java (Integer value) можно оставить без изменений, чтобы не ломать остальной код.
    @Column(name = "action_value")
    Integer value;
}