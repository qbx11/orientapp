package com.orientapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Encja zgłoszenia zawodnika na zawody w danej kategorii.
 * <p>
 * Ograniczenie unikalności (zawodnik, zawody) gwarantuje, że jeden zawodnik
 * nie zapisze się dwukrotnie na te same zawody. Wynik ({@link Result}) jest
 * uzupełniany po rozegraniu zawodów.
 */
@Entity
@Table(
    name = "registrations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"competitor_id", "event_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"competitor", "event", "category", "result"})
public class Registration {

    /** Klucz główny (autoinkrementowany). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Zawodnik, którego dotyczy zgłoszenie. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competitor_id", nullable = false)
    private AppUser competitor;

    /** Zawody, na które złożono zgłoszenie. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Kategoria (trasa), na którą zapisał się zawodnik. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Numer chipa SI używanego do pomiaru czasu. */
    private String chipNumber;

    /** Status zgłoszenia; domyślnie {@link RegistrationStatus#PENDING}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING;

    /** Moment złożenia zgłoszenia; domyślnie czas utworzenia obiektu. */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    /** Wynik zawodnika; {@code null} dopóki nie zostanie wprowadzony po zawodach. */
    @OneToOne(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Result result;
}
