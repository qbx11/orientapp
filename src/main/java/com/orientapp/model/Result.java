package com.orientapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false, unique = true)
    private Registration registration;

    @Convert(converter = DurationConverter.class)
    private Duration finishTime;

    private Integer place;

    @Column(nullable = false)
    @Builder.Default
    private Boolean dnf = false;
}
