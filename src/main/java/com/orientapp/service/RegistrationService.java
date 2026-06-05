package com.orientapp.service;

import com.orientapp.exception.DuplicateRegistrationException;
import com.orientapp.exception.EntityNotFoundException;
import com.orientapp.exception.RegistrationClosedException;
import com.orientapp.model.*;
import com.orientapp.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serwis obsługi zgłoszeń zawodników na zawody ({@link Registration}).
 * <p>
 * Odpowiada za rejestrację (w tym anonimową), zatwierdzanie i odrzucanie zgłoszeń
 * oraz za zestawienia wykorzystujące Java Collections API (filtrowanie po statusie,
 * grupowanie i zliczanie per kategoria).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final CategoryService categoryService;
    private final AppUserService userService;

    /**
     * Rejestruje zawodnika (z istniejącym kontem) na zawody.
     * <p>
     * Warunki: event musi mieć status {@link EventStatus#OPEN}, a zawodnik nie może
     * być już zapisany na te zawody.
     *
     * @param eventId      identyfikator zawodów
     * @param categoryId   identyfikator kategorii
     * @param competitorId identyfikator zawodnika
     * @param chipNumber   numer chipa SI
     * @return zapisane zgłoszenie ze statusem {@link RegistrationStatus#PENDING}
     * @throws RegistrationClosedException   gdy zawody nie są w statusie OPEN
     * @throws DuplicateRegistrationException gdy zawodnik jest już zapisany na te zawody
     * @throws EntityNotFoundException       gdy event, kategoria lub zawodnik nie istnieją
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

    /**
     * Wyszukuje zgłoszenie po identyfikatorze.
     *
     * @param id identyfikator zgłoszenia
     * @return znalezione zgłoszenie
     * @throws EntityNotFoundException gdy zgłoszenie o podanym id nie istnieje
     */
    public Registration findById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono zgłoszenia o id: " + id));
    }

    /**
     * Zwraca wszystkie zgłoszenia dla danych zawodów.
     *
     * @param eventId identyfikator zawodów
     * @return lista zgłoszeń (może być pusta)
     */
    public List<Registration> findByEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    /**
     * Zlicza wszystkie oczekujące zgłoszenia ({@link RegistrationStatus#PENDING})
     * w całej aplikacji — filtrowanie strumieniem w Javie.
     *
     * @return liczba oczekujących zgłoszeń
     */
    public long countPendingAll() {
        return registrationRepository.findAll().stream()
                .filter(r -> r.getStatus() == RegistrationStatus.PENDING)
                .count();
    }

    /**
     * Zwraca oczekujące zgłoszenia danego eventu — filtrowanie po statusie
     * {@link RegistrationStatus#PENDING} przez {@code stream().filter()} w Javie.
     *
     * @param eventId identyfikator zawodów
     * @return lista oczekujących zgłoszeń (może być pusta)
     */
    public List<Registration> findPendingByEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .filter(r -> r.getStatus() == RegistrationStatus.PENDING)
                .collect(Collectors.toList());
    }

    /**
     * Grupuje zgłoszenia danego eventu per kategoria — {@code Collectors.groupingBy} w Javie.
     *
     * @param eventId identyfikator zawodów
     * @return mapa: kategoria → lista jej zgłoszeń
     */
    public Map<Category, List<Registration>> groupByCategory(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .collect(Collectors.groupingBy(Registration::getCategory));
    }

    /**
     * Zlicza zgłoszenia per kategoria dla danego eventu.
     *
     * @param eventId identyfikator zawodów
     * @return mapa: nazwa kategorii → liczba zgłoszeń
     */
    public Map<String, Long> countByCategory(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory().getName(),
                        Collectors.counting()
                ));
    }

    /**
     * Zlicza zgłoszenia per kategoria dla wszystkich eventów (dane do wykresu dashboardu).
     *
     * @return mapa: nazwa kategorii → łączna liczba zgłoszeń
     */
    public Map<String, Long> countByCategoryAllEvents() {
        return registrationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory().getName(),
                        Collectors.counting()
                ));
    }

    /**
     * Zatwierdza zgłoszenie (status → {@link RegistrationStatus#APPROVED}).
     *
     * @param registrationId identyfikator zgłoszenia
     * @return zaktualizowane zgłoszenie
     * @throws EntityNotFoundException gdy zgłoszenie o podanym id nie istnieje
     */
    @Transactional
    public Registration approve(Long registrationId) {
        Registration reg = findById(registrationId);
        reg.setStatus(RegistrationStatus.APPROVED);
        return registrationRepository.save(reg);
    }

    /**
     * Aktualizuje dane zawodnika powiązanego ze zgłoszeniem oraz numer chipa.
     *
     * @param registrationId identyfikator zgłoszenia
     * @param firstName      imię zawodnika
     * @param lastName       nazwisko zawodnika
     * @param email          adres e-mail
     * @param phone          numer telefonu
     * @param dateOfBirth    data urodzenia
     * @param club           klub sportowy
     * @param chipNumber     numer chipa SI
     * @return zaktualizowane zgłoszenie
     */
    @Transactional
    public Registration updateCompetitor(Long registrationId, String firstName, String lastName,
                                         String email, String phone, LocalDate dateOfBirth,
                                         String club, String chipNumber) {
        Registration reg = findById(registrationId);
        AppUser competitor = reg.getCompetitor();
        String fullName = ((firstName == null ? "" : firstName.trim()) + " "
                + (lastName == null ? "" : lastName.trim())).trim();
        competitor.setFullName(fullName);
        competitor.setEmail(email == null || email.isBlank() ? null : email.trim());
        competitor.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        competitor.setDateOfBirth(dateOfBirth);
        competitor.setClub(club == null || club.isBlank() ? null : club.trim());
        reg.setChipNumber(chipNumber == null || chipNumber.isBlank() ? null : chipNumber.trim());
        return registrationRepository.save(reg);
    }

    /**
     * Odrzuca zgłoszenie (status → {@link RegistrationStatus#REJECTED}).
     *
     * @param registrationId identyfikator zgłoszenia
     * @return zaktualizowane zgłoszenie
     * @throws EntityNotFoundException gdy zgłoszenie o podanym id nie istnieje
     */
    @Transactional
    public Registration reject(Long registrationId) {
        Registration reg = findById(registrationId);
        reg.setStatus(RegistrationStatus.REJECTED);
        return registrationRepository.save(reg);
    }

    /**
     * Rejestruje zawodnika z publicznego formularza, zakładając dla niego
     * anonimowe konto (bez możliwości logowania).
     *
     * @param eventId    identyfikator zawodów
     * @param categoryId identyfikator kategorii
     * @param firstName  imię zawodnika
     * @param lastName   nazwisko zawodnika
     * @param email      adres e-mail zawodnika
     * @param phone      numer telefonu zawodnika (może być {@code null})
     * @param dateOfBirth data urodzenia zawodnika
     * @param club       klub sportowy (może być {@code null})
     * @param chipNumber numer chipa SI
     * @return zapisane zgłoszenie ze statusem {@link RegistrationStatus#PENDING}
     * @throws RegistrationClosedException gdy zawody nie są w statusie OPEN
     * @throws EntityNotFoundException     gdy event lub kategoria nie istnieją
     */
    @Transactional
    public Registration registerAnonymous(Long eventId, Long categoryId,
                                          String firstName, String lastName,
                                          String email, String phone, LocalDate dateOfBirth,
                                          String club, String chipNumber) {
        Event event = eventService.findById(eventId);
        if (event.getStatus() != EventStatus.OPEN) {
            throw new RegistrationClosedException(event.getName());
        }
        AppUser competitor = userService.createAnonymousCompetitor(
                firstName + " " + lastName, email, phone, dateOfBirth, club);
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
