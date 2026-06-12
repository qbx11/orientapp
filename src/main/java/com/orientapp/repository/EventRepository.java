package com.orientapp.repository;

import com.orientapp.model.Event;
import com.orientapp.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozytorium zawodów ({@link Event}).
 * <p>
 * Dziedziczy operacje CRUD z {@link JpaRepository} i dodaje filtrowanie
 * po statusie zawodów.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Zwraca zawody o podanym statusie.
     *
     * @param status status zawodów (DRAFT / OPEN / CLOSED)
     * @return lista pasujących zawodów (może być pusta)
     */
    List<Event> findByStatus(EventStatus status);
}
