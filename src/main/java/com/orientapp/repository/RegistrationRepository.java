package com.orientapp.repository;

import com.orientapp.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEventId(Long eventId);
    List<Registration> findByCompetitorId(Long competitorId);
    boolean existsByCompetitorIdAndEventId(Long competitorId, Long eventId);
}
