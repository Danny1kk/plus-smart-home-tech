package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Action {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    Long id;

    //@Column(name = "type", columnDefinition = "VARCHAR(255)")
    @Enumerated(EnumType.STRING)
    ActionTypeAvro type;

    //@Column(name = "action_value")
    Integer value;
}