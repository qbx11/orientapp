package com.orientapp.repository;

import com.orientapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozytorium kategorii/tras ({@link Category}).
 * <p>
 * Dziedziczy operacje CRUD z {@link JpaRepository} i dodaje wyszukiwanie
 * kategorii należących do danych zawodów.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Zwraca kategorie przypisane do zawodów o podanym identyfikatorze.
     *
     * @param eventId identyfikator zawodów
     * @return lista kategorii (może być pusta)
     */
    List<Category> findByEventId(Long eventId);
}
