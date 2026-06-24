package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scenario_actions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScenarioAction {
    @EmbeddedId
    private ScenarioActionId id;

    @MapsId("scenarioId")
    @ManyToOne
    private Scenario scenario;

    @MapsId("sensorId")
    @ManyToOne
    private Sensor sensor;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "action_id")
    private Action action;
}