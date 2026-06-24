package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

@Entity
@Table(name = "actions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    ActionTypeAvro type;

    Integer value;
}