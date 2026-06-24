package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Condition {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    Long id;

//    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    ConditionTypeAvro type;

//    @Column(name = "operation")
    @Enumerated(EnumType.STRING)
    ConditionOperationAvro operation;

//    @Column(name = "value")
    Integer value;
}