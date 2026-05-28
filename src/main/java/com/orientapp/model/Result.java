package com.orientapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;

/**
 * Encja wyniku zawodnika powiązana ze zgłoszeniem ({@link Registration}).
 * <p>
 * Miejsce ({@link #place}) wyliczane jest dynamicznie na podstawie czasu
 * ukończenia w obrębie kategorii. Zawodnik z flagą DNF nie otrzymuje miejsca.
 */
@Entity
@Table(name = "results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "registration")
public class Result {

    /** Klucz główny (autoinkrementowany). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Zgłoszenie, którego dotyczy wynik (relacja jeden-do-jednego). */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false, unique = true)
    private Registration registration;

    /** Czas ukończenia trasy; {@code null} dla zawodnika DNF. */
    @Convert(converter = DurationConverter.class)
    private Duration finishTime;

    /** Miejsce w kategorii; {@code null} dla zawodnika DNF. */
    private Integer place;

    /** Flaga „nie ukończył” (Did Not Finish); domyślnie {@code false}. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean dnf = false;
}
