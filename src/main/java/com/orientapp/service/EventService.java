package com.orientapp.service;

import com.orientapp.exception.EntityNotFoundException;
import com.orientapp.model.Event;
import com.orientapp.model.EventStatus;
import com.orientapp.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Zwraca nadchodzące zawody (status OPEN, data >= dziś), posortowane rosnąco po dacie.
     * Sortowanie odbywa się w Javie, nie w JPQL.
     */
    public List<Event> findUpcoming() {
        LocalDate today = LocalDate.now();
        return eventRepository.findAll().stream()
                .filter(e -> e.getStatus() != EventStatus.CLOSED)
                .filter(e -> !e.getDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getDate))
                .collect(Collectors.toList());
    }

    public List<Event> findAll() {
        return eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getDate).reversed())
                .collect(Collectors.toList());
    }

    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono zawodów o id: " + id));
    }

    @Transactional
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Transactional
    public Event update(Long id, Event updated) {
        Event event = findById(id);
        event.setName(updated.getName());
        event.setDate(updated.getDate());
        event.setLocation(updated.getLocation());
        event.setLatitude(updated.getLatitude());
        event.setLongitude(updated.getLongitude());
        event.setDescription(updated.getDescription());
        event.setStatus(updated.getStatus());
        return eventRepository.save(event);
    }

    @Transactional
    public void changeStatus(Long id, EventStatus newStatus) {
        Event event = findById(id);
        event.setStatus(newStatus);
        eventRepository.save(event);
    }

    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Nie znaleziono zawodów o id: " + id);
        }
        eventRepository.deleteById(id);
    }
}
