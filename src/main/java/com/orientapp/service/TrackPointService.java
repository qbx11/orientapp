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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackPointService {

    private final TrackPointRepository trackPointRepository;
    private final RegistrationRepository registrationRepository;

    /** Zwraca punkty trasy posortowane po checkpointOrder, a przy braku — po timestamp. */
    public List<TrackPoint> findByRegistration(Long registrationId) {
        List<TrackPoint> points = trackPointRepository
                .findByRegistrationIdOrderByTimestampAsc(registrationId);
        return points.stream()
                .sorted(Comparator.comparing(
                        tp -> tp.getCheckpointOrder() != null ? tp.getCheckpointOrder() : Integer.MAX_VALUE))
                .toList();
    }

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

    @Transactional
    public void deletePoint(Long pointId) {
        if (!trackPointRepository.existsById(pointId)) {
            throw new EntityNotFoundException("Punkt trasy o id " + pointId + " nie istnieje.");
        }
        trackPointRepository.deleteById(pointId);
    }

    @Transactional
    public void deleteByRegistration(Long registrationId) {
        trackPointRepository.deleteByRegistrationId(registrationId);
    }
}
