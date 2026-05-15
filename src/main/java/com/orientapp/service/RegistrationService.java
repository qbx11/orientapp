package com.orientapp.service;

import com.orientapp.exception.DuplicateRegistrationException;
import com.orientapp.exception.EntityNotFoundException;
import com.orientapp.exception.RegistrationClosedException;
import com.orientapp.model.*;
import com.orientapp.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final CategoryService categoryService;
    private final AppUserService userService;

    /**
     * Rejestruje zawodnika na zawody. Sprawdza:
     * - event musi mieć status OPEN
     * - zawodnik nie może być już zapisany na te zawody
     */
    @Transactional
    public Registration register(Long eventId, Long categoryId, Long competitorId, String chipNumber) {
        Event event = eventService.findById(eventId);

        if (event.getStatus() != EventStatus.OPEN) {
            throw new RegistrationClosedException(event.getName());
        }
        if (registrationRepository.existsByCompetitorIdAndEventId(competitorId, eventId)) {
            AppUser user = userService.findById(competitorId);
            throw new DuplicateRegistrationException(user.getFullName(), event.getName());
        }

        Category category = categoryService.findById(categoryId);
        AppUser competitor = userService.findById(competitorId);

        Registration registration = Registration.builder()
                .event(event)
                .category(category)
                .competitor(competitor)
                .chipNumber(chipNumber)
                .build();
        return registrationRepository.save(registration);
    }

    public Registration findById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono zgłoszenia o id: " + id));
    }

    public List<Registration> findByEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    public long countPendingAll() {
        return registrationRepository.findAll().stream()
                .filter(r -> r.getStatus() == RegistrationStatus.PENDING)
                .count();
    }

    /**
     * Filtruje zgłoszenia po statusie PENDING — stream().filter() w Javie.
     */
    public List<Registration> findPendingByEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .filter(r -> r.getStatus() == RegistrationStatus.PENDING)
                .collect(Collectors.toList());
    }

    /**
     * Grupuje zgłoszenia danego eventu per kategoria — Collectors.groupingBy w Javie.
     */
    public Map<Category, List<Registration>> groupByCategory(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .collect(Collectors.groupingBy(Registration::getCategory));
    }

    /**
     * Zwraca liczbę zgłoszeń per kategoria dla danego eventu.
     */
    public Map<String, Long> countByCategory(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory().getName(),
                        Collectors.counting()
                ));
    }

    /**
     * Zwraca liczbę zgłoszeń per kategoria dla wszystkich eventów (do wykresu dashboardu).
     */
    public Map<String, Long> countByCategoryAllEvents() {
        return registrationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory().getName(),
                        Collectors.counting()
                ));
    }

    @Transactional
    public Registration approve(Long registrationId) {
        Registration reg = findById(registrationId);
        reg.setStatus(RegistrationStatus.APPROVED);
        return registrationRepository.save(reg);
    }

    @Transactional
    public Registration reject(Long registrationId) {
        Registration reg = findById(registrationId);
        reg.setStatus(RegistrationStatus.REJECTED);
        return registrationRepository.save(reg);
    }

    /** Rejestracja anonimowa z publicznego formularza — tworzy użytkownika bez konta. */
    @Transactional
    public Registration registerAnonymous(Long eventId, Long categoryId,
                                          String firstName, String lastName,
                                          String club, String chipNumber) {
        Event event = eventService.findById(eventId);
        if (event.getStatus() != EventStatus.OPEN) {
            throw new RegistrationClosedException(event.getName());
        }
        AppUser competitor = userService.createAnonymousCompetitor(firstName + " " + lastName, club);
        Category category = categoryService.findById(categoryId);
        Registration reg = Registration.builder()
                .event(event)
                .category(category)
                .competitor(competitor)
                .chipNumber(chipNumber)
                .build();
        return registrationRepository.save(reg);
    }
}
