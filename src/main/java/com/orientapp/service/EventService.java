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

/**
 * Serwis zarządzania zawodami ({@link Event}).
 * <p>
 * Udostępnia operacje CRUD oraz logikę prezentacji listy zawodów (sortowanie
 * po dacie, rozdział na nadchodzące i zakończone). Sortowanie i filtrowanie
 * wykonywane są celowo w Javie, a nie w zapytaniach JPQL.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Zwraca zawody w kolejności prezentacji: najpierw nadchodzące (data &gt;= dziś)
     * rosnąco po dacie, następnie zakończone malejąco po dacie.
     * Sortowanie odbywa się w Javie, nie w JPQL.
     *
     * @return uporządkowana lista zawodów (może być pusta)
     */
    public List<Event> findUpcoming() {
        LocalDate today = LocalDate.now();
        // Przyszłe (OPEN/DRAFT) rosnąco, potem zakończone malejąco
        List<Event> upcoming = eventRepository.findAll().stream()
                .filter(e -> !e.getDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getDate))
                .collect(Collectors.toList());
        List<Event> past = eventRepository.findAll().stream()
                .filter(e -> e.getDate().isBefore(today) || e.getStatus() == EventStatus.CLOSED)
                .sorted(Comparator.comparing(Event::getDate).reversed())
                .collect(Collectors.toList());
        upcoming.addAll(past);
        return upcoming.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Zwraca wszystkie zawody posortowane malejąco po dacie (najnowsze pierwsze).
     *
     * @return lista zawodów (może być pusta)
     */
    public List<Event> findAll() {
        return eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Wyszukuje zawody po identyfikatorze.
     *
     * @param id identyfikator zawodów
     * @return znalezione zawody
     * @throws EntityNotFoundException gdy zawody o podanym id nie istnieją
     */
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono zawodów o id: " + id));
    }

    /**
     * Zapisuje nowe zawody.
     *
     * @param event zawody do zapisania
     * @return zapisane zawody (z nadanym identyfikatorem)
     */
    @Transactional
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    /**
     * Aktualizuje dane istniejących zawodów wartościami z przekazanego obiektu.
     *
     * @param id      identyfikator aktualizowanych zawodów
     * @param updated obiekt z nowymi wartościami pól
     * @return zaktualizowane zawody
     * @throws EntityNotFoundException gdy zawody o podanym id nie istnieją
     */
    @Transactional
    public Event update(Long id, Event updated) {
        Event event = findById(id);
        event.setName(updated.getName());
        event.setDate(updated.getDate());
        event.setLocation(updated.getLocation());
        event.setLatitude(updated.getLatitude());
        event.setLongitude(updated.getLongitude());
        event.setDescription(updated.getDescription());
        event.setRegulations(updated.getRegulations());
        event.setMaxParticipants(updated.getMaxParticipants());
        event.setStatus(updated.getStatus());
        return eventRepository.save(event);
    }

    /**
     * Zmienia status zawodów (np. DRAFT → OPEN → CLOSED).
     *
     * @param id        identyfikator zawodów
     * @param newStatus nowy status
     * @throws EntityNotFoundException gdy zawody o podanym id nie istnieją
     */
    @Transactional
    public void changeStatus(Long id, EventStatus newStatus) {
        Event event = findById(id);
        event.setStatus(newStatus);
        eventRepository.save(event);
    }

    /**
     * Usuwa zawody o podanym identyfikatorze.
     *
     * @param id identyfikator zawodów
     * @throws EntityNotFoundException gdy zawody o podanym id nie istnieją
     */
    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Nie znaleziono zawodów o id: " + id);
        }
        eventRepository.deleteById(id);
    }
}
