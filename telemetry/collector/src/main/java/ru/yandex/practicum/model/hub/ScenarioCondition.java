package ru.yandex.practicum.model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.enums.ConditionOperation;
import ru.yandex.practicum.enums.ScenarioConditionType;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    @NotBlank
    private String sensorId;

    @NotNull
    private ScenarioConditionType type;

    @NotNull
    private ConditionOperation operation;

    @NotNull
    private Integer value;
}