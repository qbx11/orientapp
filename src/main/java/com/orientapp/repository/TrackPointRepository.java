package com.orientapp.repository;

import com.orientapp.model.TrackPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackPointRepository extends JpaRepository<TrackPoint, Long> {

    List<TrackPoint> findByRegistrationIdOrderByCheckpointOrderAsc(Long registrationId);

    List<TrackPoint> findByRegistrationIdOrderByTimestampAsc(Long registrationId);

    void deleteByRegistrationId(Long registrationId);
}
