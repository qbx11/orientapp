package com.orientapp.service;

import com.orientapp.exception.EntityNotFoundException;
import com.orientapp.model.AppUser;
import com.orientapp.model.Role;
import com.orientapp.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serwis zarządzania użytkownikami aplikacji ({@link AppUser}).
 * <p>
 * Implementuje {@link UserDetailsService}, dzięki czemu Spring Security
 * pobiera tożsamość użytkownika przy logowaniu. Odpowiada również za tworzenie
 * kont (z hasłem szyfrowanym {@link PasswordEncoder}) oraz anonimowych
 * zawodników zakładanych z publicznego formularza rejestracji.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppUserService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Ładuje dane użytkownika na potrzeby uwierzytelniania Spring Security.
     *
     * @param username login użytkownika (adres email)
     * @return dane użytkownika ({@link UserDetails})
     * @throws UsernameNotFoundException gdy użytkownik o podanym loginie nie istnieje
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));
    }

    /**
     * Wyszukuje użytkownika po identyfikatorze.
     *
     * @param id identyfikator użytkownika
     * @return znaleziony użytkownik
     * @throws EntityNotFoundException gdy użytkownik o podanym id nie istnieje
     */
    public AppUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika o id: " + id));
    }

    /**
     * Zwraca wszystkich użytkowników aplikacji.
     *
     * @return lista użytkowników (może być pusta)
     */
    public List<AppUser> findAll() {
        return userRepository.findAll();
    }

    /**
     * Tworzy nowego użytkownika z zaszyfrowanym hasłem.
     *
     * @param username    login (adres email), musi być unikalny
     * @param rawPassword hasło w postaci jawnej (zostanie zaszyfrowane)
     * @param fullName    imię i nazwisko
     * @param club        klub sportowy (może być {@code null})
     * @param role        rola użytkownika
     * @return zapisany użytkownik
     * @throws IllegalArgumentException gdy użytkownik o podanym loginie już istnieje
     */
    @Transactional
    public AppUser createUser(String username, String rawPassword, String fullName, String club, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Użytkownik o emailu " + username + " już istnieje.");
        }
        AppUser user = AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .club(club)
                .role(role)
                .build();
        return userRepository.save(user);
    }

    /**
     * Tworzy anonimowego zawodnika z publicznego formularza rejestracji (bez konta).
     * <p>
     * Login generowany jest losowo (UUID), hasło pozostaje puste — takie konto
     * służy wyłącznie do powiązania zgłoszenia z osobą i nie pozwala na logowanie.
     *
     * @param fullName imię i nazwisko zawodnika
     * @param club     klub sportowy (może być {@code null})
     * @return zapisany użytkownik o roli {@link Role#COMPETITOR}
     */
    @Transactional
    public AppUser createAnonymousCompetitor(String fullName, String club) {
        String username = UUID.randomUUID() + "@anon.orientapp.pl";
        return userRepository.save(AppUser.builder()
                .username(username)
                .passwordHash("")
                .fullName(fullName)
                .club(club)
                .role(Role.COMPETITOR)
                .build());
    }

    /**
     * Usuwa użytkownika o podanym identyfikatorze.
     *
     * @param id identyfikator użytkownika
     * @throws EntityNotFoundException gdy użytkownik o podanym id nie istnieje
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Nie znaleziono użytkownika o id: " + id);
        }
        userRepository.deleteById(id);
    }
}
