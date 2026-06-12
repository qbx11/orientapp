package com.orientapp.repository;

import com.orientapp.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repozytorium wyników ({@link Result}).
 * <p>
 * Dziedziczy operacje CRUD z {@link JpaRepository} i udostępnia wyszukiwanie
 * wyników po kategorii, po zawodach oraz po pojedynczym zgłoszeniu. Nawigacja
 * po polach zagnieżdżonych (np. {@code registration.category.id}) realizowana
 * jest mechanizmem nazw metod Spring Data.
 */
public interface ResultRepository extends JpaRepository<Result, Long> {

    /**
     * Zwraca wyniki dla kategorii o podanym identyfikatorze.
     *
     * @param categoryId identyfikator kategorii
     * @return lista wyników (może być pusta)
     */
    List<Result> findByRegistrationCategoryId(Long categoryId);

    /**
     * Zwraca wszystkie wyniki dla zawodów o podanym identyfikatorze.
     *
     * @param eventId identyfikator zawodów
     * @return lista wyników (może być pusta)
     */
    List<Result> findByRegistrationEventId(Long eventId);

    /**
     * Zwraca wynik powiązany z podanym zgłoszeniem.
     *
     * @param registrationId identyfikator zgłoszenia
     * @return {@link Optional} z wynikiem lub pusty, gdy wynik nie istnieje
     */
    Optional<Result> findByRegistrationId(Long registrationId);
}
