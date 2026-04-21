package com.orientapp.repository;

import com.orientapp.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByRegistrationCategoryId(Long categoryId);
    List<Result> findByRegistrationEventId(Long eventId);
    Optional<Result> findByRegistrationId(Long registrationId);
}
