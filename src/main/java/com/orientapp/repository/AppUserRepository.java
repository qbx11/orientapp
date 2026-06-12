package com.orientapp.repository;

import com.orientapp.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozytorium użytkowników aplikacji ({@link AppUser}).
 * <p>
 * Dziedziczy standardowe operacje CRUD z {@link JpaRepository} i dodaje
 * wyszukiwanie po nazwie użytkownika (która pełni rolę loginu/e-maila).
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Wyszukuje użytkownika po nazwie (loginie).
     *
     * @param username nazwa użytkownika (e-mail)
     * @return {@link Optional} z użytkownikiem lub pusty, gdy nie istnieje
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Sprawdza, czy istnieje użytkownik o podanej nazwie.
     *
     * @param username nazwa użytkownika (e-mail)
     * @return {@code true}, gdy użytkownik istnieje
     */
    boolean existsByUsername(String username);
}
