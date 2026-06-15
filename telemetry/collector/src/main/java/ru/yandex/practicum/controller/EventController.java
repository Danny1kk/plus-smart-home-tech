package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.service.EventService;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;


    @PostMapping("/sensors")
    public void createSensorEvent(@Valid @RequestBody SensorEvent event) {
        eventService.createSensorEvent(event);
    }

    @PostMapping("/hubs")
    public void createHubEvent(@Valid @RequestBody HubEvent hubEvent) {
        eventService.createHubEvent(hubEvent);
    }
}