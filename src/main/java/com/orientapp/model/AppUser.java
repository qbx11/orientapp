package com.orientapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Encja użytkownika aplikacji — administratora lub zawodnika.
 * <p>
 * Implementuje {@link UserDetails}, dzięki czemu obiekt jest bezpośrednio
 * używany przez Spring Security jako tożsamość zalogowanego użytkownika.
 * Zawodnicy zgłaszani z publicznego formularza są zapisywani jako konta
 * anonimowe (z pustym hasłem).
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "registrations")
public class AppUser implements UserDetails {

    /** Klucz główny (autoinkrementowany). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Login użytkownika — adres email, unikalny w skali aplikacji. */
    @Column(nullable = false, unique = true)
    private String username;

    /** Hasło zaszyfrowane algorytmem BCrypt (puste dla kont anonimowych). */
    @Column(nullable = false)
    private String passwordHash;

    /** Imię i nazwisko użytkownika. */
    @Column(nullable = false)
    private String fullName;

    /** Klub sportowy zawodnika (opcjonalny). */
    private String club;

    /** Rola decydująca o uprawnieniach ({@link Role#ADMIN} lub {@link Role#COMPETITOR}). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Zgłoszenia złożone przez tego zawodnika. */
    @OneToMany(mappedBy = "competitor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Registration> registrations = new java.util.ArrayList<>();

    /**
     * Zwraca uprawnienia użytkownika na podstawie jego roli.
     *
     * @return jednoelementowa kolekcja z uprawnieniem {@code ROLE_<rola>}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Zwraca zaszyfrowane hasło na potrzeby Spring Security.
     *
     * @return hash hasła
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }
}
