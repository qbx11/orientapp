package com.orientapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Encja kategorii (trasy) w ramach zawodów, np. „TP12”, „TR30”.
 * <p>
 * Każda kategoria należy do jednego {@link Event} i grupuje zgłoszenia
 * zawodników startujących na tej samej trasie.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"event", "registrations"})
public class Category {

    /** Klucz główny (autoinkrementowany). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nazwa kategorii (np. „TP12”). */
    @Column(nullable = false)
    private String name;

    /** Długość trasy w kilometrach (opcjonalna). */
    private Double distanceKm;

    /** Zawody, do których należy kategoria. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Zgłoszenia zawodników w tej kategorii. */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();
}
