package ru.yandex.practicum.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scenario_conditions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScenarioCondition {
    @EmbeddedId
    ScenarioConditionId id;

    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    Scenario scenario;

    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    Sensor sensor;

    @MapsId("conditionId")
    @JoinColumn(name = "condition_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    Condition condition;
}