package com.orientapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja zawodów w biegu na orientację — centralny obiekt domeny aplikacji.
 * <p>
 * Zawody agregują kategorie (trasy) oraz zgłoszenia zawodników. Współrzędne
 * geograficzne wykorzystywane są m.in. do pobrania pogody i prezentacji na mapie.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"categories", "registrations"})
public class Event {

    /** Klucz główny (autoinkrementowany). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nazwa zawodów (np. „SNOB 2026”). */
    @Column(nullable = false)
    private String name;

    /** Data rozegrania zawodów. */
    @Column(nullable = false)
    private LocalDate date;

    /** Nazwa miejscowości, w której odbywają się zawody. */
    @Column(nullable = false)
    private String location;

    /** Szerokość geograficzna lokalizacji (do pogody i mapy). */
    private Double latitude;

    /** Długość geograficzna lokalizacji (do pogody i mapy). */
    private Double longitude;

    /** Opis zawodów. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Treść regulaminu widoczna dla uczestników. */
    @Column(columnDefinition = "TEXT")
    private String regulations;

    /** Status zawodów; domyślnie {@link EventStatus#DRAFT}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    /** Kategorie (trasy) zdefiniowane w ramach zawodów. */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    /** Zgłoszenia zawodników na te zawody. */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();
}
