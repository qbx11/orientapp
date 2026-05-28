package com.orientapp.service;

import com.orientapp.exception.EntityNotFoundException;
import com.orientapp.model.Registration;
import com.orientapp.model.TrackPoint;
import com.orientapp.repository.RegistrationRepository;
import com.orientapp.repository.TrackPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Serwis zarządzania punktami trasy GPS ({@link TrackPoint}) powiązanymi ze zgłoszeniem.
 * <p>
 * Punkty reprezentują kolejne pozycje zawodnika na trasie. Serwis udostępnia ich
 * dodawanie, usuwanie oraz pobieranie w kolejności przejścia (po {@code checkpointOrder},
 * a przy jego braku — po znaczniku czasu).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackPointService {

    private final TrackPointRepository trackPointRepository;
    private final RegistrationRepository registrationRepository;

    /**
     * Zwraca punkty trasy danego zgłoszenia posortowane po {@code checkpointOrder},
     * a przy jego braku — po znaczniku czasu. Sortowanie w Javie ({@link Comparator}).
     *
     * @param registrationId identyfikator zgłoszenia
     * @return uporządkowana lista punktów trasy (może być pusta)
     */
    public List<TrackPoint> findByRegistration(Long registrationId) {
        List<TrackPoint> points = trackPointRepository
                .findByRegistrationIdOrderByTimestampAsc(registrationId);
        return points.stream()
                .sorted(Comparator.comparing(
                        tp -> tp.getCheckpointOrder() != null ? tp.getCheckpointOrder() : Integer.MAX_VALUE))
                .toList();
    }

    /**
     * Dodaje punkt trasy do wskazanego zgłoszenia.
     *
     * @param registrationId identyfikator zgłoszenia
     * @param latitude        szerokość geograficzna punktu
     * @param longitude       długość geograficzna punktu
     * @param timestamp       znacznik czasu; gdy {@code null}, użyty zostaje bieżący czas
     * @param checkpointOrder kolejność punktu kontrolnego (może być {@code null})
     * @return zapisany punkt trasy
     * @throws EntityNotFoundException gdy zgłoszenie o podanym id nie istnieje
     */
    @Transactional
    public TrackPoint addPoint(Long registrationId, Double latitude, Double longitude,
                               LocalDateTime timestamp, Integer checkpointOrder) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie znaleziono zgłoszenia o id: " + registrationId));
        TrackPoint point = TrackPoint.builder()
                .registration(registration)
                .latitude(latitude)
                .longitude(longitude)
                .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
                .checkpointOrder(checkpointOrder)
                .build();
        return trackPointRepository.save(point);
    }

    /**
     * Usuwa pojedynczy punkt trasy.
     *
     * @param pointId identyfikator punktu trasy
     * @throws EntityNotFoundException gdy punkt o podanym id nie istnieje
     */
    @Transactional
    public void deletePoint(Long pointId) {
        if (!trackPointRepository.existsById(pointId)) {
            throw new EntityNotFoundException("Punkt trasy o id " + pointId + " nie istnieje.");
        }
        trackPointRepository.deleteById(pointId);
    }

    /**
     * Usuwa wszystkie punkty trasy powiązane z danym zgłoszeniem.
     *
     * @param registrationId identyfikator zgłoszenia
     */
    @Transactional
    public void deleteByRegistration(Long registrationId) {
        trackPointRepository.deleteByRegistrationId(registrationId);
    }
}
