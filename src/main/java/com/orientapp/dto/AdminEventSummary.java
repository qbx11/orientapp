package com.orientapp.dto;

import com.orientapp.model.Event;

/**
 * Podsumowanie zawodów na dashboardzie administratora — zawody wraz z policzonymi
 * statystykami zgłoszeń. Liczby wyznaczane są w warstwie serwisowej/kontrolera
 * (filtrowanie strumieniem), aby widok pozostał pozbawiony logiki.
 *
 * @param event       zawody, których dotyczy podsumowanie
 * @param pending     liczba zgłoszeń oczekujących na decyzję (PENDING)
 * @param approved    liczba zgłoszeń zatwierdzonych (APPROVED)
 * @param total       łączna liczba zgłoszeń na te zawody
 * @param categories  liczba zdefiniowanych kategorii (tras)
 * @param withResults liczba zgłoszeń z wprowadzonym wynikiem
 */
public record AdminEventSummary(
        Event event,
        long pending,
        long approved,
        long total,
        int categories,
        long withResults
) {
}
