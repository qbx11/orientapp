package com.orientapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Encja pojedynczego punktu trasy GPS przebytej przez zawodnika.
 * <p>
 * Punkty powiązane są ze zgłoszeniem ({@link Registration}) i odtwarzają
 * przebieg trasy zawodnika w kolejności wyznaczonej przez {@link #checkpointOrder}
 * (lub znacznik czasu, gdy kolejność nie jest podana).
 */
@Entity
@Table(name = "track_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "registration")
public class TrackPoint {

    /** Klucz główny (autoinkrementowany). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Zgłoszenie, do którego należy punkt trasy. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    /** Szerokość geograficzna punktu. */
    @Column(nullable = false)
    private Double latitude;

    /** Długość geograficzna punktu. */
    @Column(nullable = false)
    private Double longitude;

    /** Znacznik czasu zarejestrowania punktu. */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** Kolejność punktu kontrolnego na trasie (opcjonalna). */
    private Integer checkpointOrder;
}
