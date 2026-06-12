package com.orientapp.repository;

import com.orientapp.model.TrackPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozytorium punktów trasy GPS ({@link TrackPoint}).
 * <p>
 * Dziedziczy operacje CRUD z {@link JpaRepository} i udostępnia pobieranie
 * punktów trasy danego zgłoszenia w kolejności kontrolnej lub czasowej oraz
 * usuwanie całej trasy.
 */
public interface TrackPointRepository extends JpaRepository<TrackPoint, Long> {

    /**
     * Zwraca punkty trasy zgłoszenia uporządkowane rosnąco po numerze punktu kontrolnego.
     *
     * @param registrationId identyfikator zgłoszenia
     * @return uporządkowana lista punktów trasy (może być pusta)
     */
    List<TrackPoint> findByRegistrationIdOrderByCheckpointOrderAsc(Long registrationId);

    /**
     * Zwraca punkty trasy zgłoszenia uporządkowane rosnąco po znaczniku czasu.
     *
     * @param registrationId identyfikator zgłoszenia
     * @return uporządkowana lista punktów trasy (może być pusta)
     */
    List<TrackPoint> findByRegistrationIdOrderByTimestampAsc(Long registrationId);

    /**
     * Usuwa wszystkie punkty trasy powiązane z danym zgłoszeniem.
     *
     * @param registrationId identyfikator zgłoszenia
     */
    void deleteByRegistrationId(Long registrationId);
}
