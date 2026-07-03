package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Entity
@Table(name = "conditions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    ConditionTypeAvro type;

    @Enumerated(EnumType.STRING)
    ConditionOperationAvro operation;

    @Column(name = "field")
    private String field;

    @Column(name = "value")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;
}