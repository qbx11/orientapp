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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppUserService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));
    }

    public AppUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika o id: " + id));
    }

    public List<AppUser> findAll() {
        return userRepository.findAll();
    }

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

    /** Tworzy anonimowego zawodnika z publicznego formularza rejestracji (bez konta). */
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

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Nie znaleziono użytkownika o id: " + id);
        }
        userRepository.deleteById(id);
    }
}
