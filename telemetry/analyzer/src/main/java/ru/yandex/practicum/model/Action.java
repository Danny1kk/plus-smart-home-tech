package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Action {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
    Long id;

    //@Column(name = "type", columnDefinition = "VARCHAR(255)")
    @Enumerated(EnumType.STRING)
    ActionTypeAvro type;

    //@Column(name = "action_value")
    Integer value;
}