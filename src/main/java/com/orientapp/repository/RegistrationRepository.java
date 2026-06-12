package com.orientapp.repository;

import com.orientapp.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozytorium zgłoszeń ({@link Registration}).
 * <p>
 * Dziedziczy operacje CRUD z {@link JpaRepository} i dodaje wyszukiwanie
 * zgłoszeń po zawodach oraz po zawodniku, a także sprawdzanie duplikatów.
 */
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    /**
     * Zwraca zgłoszenia na zawody o podanym identyfikatorze.
     *
     * @param eventId identyfikator zawodów
     * @return lista zgłoszeń (może być pusta)
     */
    List<Registration> findByEventId(Long eventId);

    /**
     * Zwraca zgłoszenia danego zawodnika.
     *
     * @param competitorId identyfikator zawodnika ({@link com.orientapp.model.AppUser})
     * @return lista zgłoszeń (może być pusta)
     */
    List<Registration> findByCompetitorId(Long competitorId);

    /**
     * Sprawdza, czy zawodnik jest już zapisany na dane zawody (ochrona przed duplikatem).
     *
     * @param competitorId identyfikator zawodnika
     * @param eventId      identyfikator zawodów
     * @return {@code true}, gdy zgłoszenie już istnieje
     */
    boolean existsByCompetitorIdAndEventId(Long competitorId, Long eventId);
}
