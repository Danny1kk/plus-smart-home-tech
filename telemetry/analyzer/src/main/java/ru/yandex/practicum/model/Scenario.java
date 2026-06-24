package ru.yandex.practicum.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "scenarios")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "hub_id")
    String hubId;

    @Column(name = "name")
    String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    List<ScenarioCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    Set<ScenarioAction> actions = new HashSet<>();

    public void setConditions(List<ScenarioCondition> conditions) {
        this.conditions.clear();
        if (conditions != null) {
            conditions.forEach(c -> {
                c.setScenario(this);
                this.conditions.add(c);
            });
        }
    }

    public void setActions(Set<ScenarioAction> actions) {
        this.actions.clear();
        if (actions != null) {
            actions.forEach(a -> {
                a.setScenario(this);
                this.actions.add(a);
            });
        }
    }
}