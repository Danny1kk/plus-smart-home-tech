package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conditions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScenarioCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private String sensorId;

    @Column(name = "type")
    private String type;

    @Column(name = "operation")
    private String operation;

    @Column(name = "field")
    private String field;

    @Column(name = "value")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    @ToString.Exclude
    private Scenario scenario;
}