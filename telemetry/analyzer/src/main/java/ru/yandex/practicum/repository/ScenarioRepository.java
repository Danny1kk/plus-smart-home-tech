package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Scenario;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    @EntityGraph(attributePaths = {"conditions", "actions"})
    @Query("SELECT s FROM Scenario s WHERE s.hubId = :hubId")
    List<Scenario> findByHubId(@Param("hubId") String hubId);

    @Query("SELECT s FROM Scenario s WHERE s.hubId = :hubId AND s.name = :name")
    Optional<Scenario> findByHubIdAndName(@Param("hubId") String hubId, @Param("name") String name);

}