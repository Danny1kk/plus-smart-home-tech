package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.model.ScenarioConditionId;

import java.util.List;

@Repository
public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {

    List<ScenarioCondition> findByScenarioId(Long scenarioId);

    List<ScenarioCondition> findByScenario(Scenario scenario);

    @Modifying
    @Query("DELETE FROM ScenarioCondition sc WHERE sc.id.scenarioId = :scenarioId")
    void deleteByScenarioId(@Param("scenarioId") Long scenarioId);
}