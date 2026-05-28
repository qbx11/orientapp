package com.orientapp.service;

import com.orientapp.exception.EntityNotFoundException;
import com.orientapp.model.Category;
import com.orientapp.model.Registration;
import com.orientapp.model.Result;
import com.orientapp.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Serwis zarządzania wynikami zawodów ({@link Result}).
 * <p>
 * Odpowiada za zapis wyników poszczególnych zgłoszeń, sortowanie ich po czasie
 * ukończenia (zawodnicy DNF zawsze na końcu) oraz przeliczanie miejsc w kategorii.
 * Sortowanie i grupowanie wykonywane są w Javie ({@link Comparator},
 * {@link Collectors#groupingBy}).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultService {

    private final ResultRepository resultRepository;
    private final RegistrationService registrationService;
    private final CategoryService categoryService;

    /**
     * Zwraca wyniki danej kategorii posortowane rosnąco po czasie ukończenia.
     * Zawodnicy DNF trafiają na koniec listy. Sortowanie w Javie ({@link Comparator}).
     *
     * @param categoryId identyfikator kategorii
     * @return posortowana lista wyników (może być pusta)
     */
    public List<Result> findByCategorySorted(Long categoryId) {
        return resultRepository.findByRegistrationCategoryId(categoryId).stream()
                .sorted(Comparator
                        .comparing(Result::getDnf)
                        .thenComparing(r -> r.getFinishTime() != null
                                ? r.getFinishTime()
                                : Duration.ofSeconds(Long.MAX_VALUE / 2)))
                .collect(Collectors.toList());
    }

    /**
     * Grupuje wyniki eventu per kategoria — {@link Collectors#groupingBy} w Javie.
     * Każda lista jest posortowana po czasie ukończenia (DNF na końcu).
     *
     * @param eventId identyfikator zawodów
     * @return mapa: kategoria → posortowana lista wyników
     */
    public Map<Category, List<Result>> findByEventGroupedByCategory(Long eventId) {
        return resultRepository.findByRegistrationEventId(eventId).stream()
                .sorted(Comparator
                        .comparing(Result::getDnf)
                        .thenComparing(r -> r.getFinishTime() != null
                                ? r.getFinishTime()
                                : Duration.ofSeconds(Long.MAX_VALUE / 2)))
                .collect(Collectors.groupingBy(r -> r.getRegistration().getCategory()));
    }

    /**
     * Zapisuje (lub aktualizuje) wynik dla zgłoszenia i przelicza miejsca w jego kategorii.
     * Dla zawodnika DNF czas ukończenia jest zerowany.
     *
     * @param registrationId identyfikator zgłoszenia
     * @param finishTime      czas ukończenia (ignorowany gdy {@code dnf == true})
     * @param dnf             {@code true} jeśli zawodnik nie ukończył (Did Not Finish)
     * @return zapisany wynik
     * @throws EntityNotFoundException gdy zgłoszenie o podanym id nie istnieje
     */
    @Transactional
    public Result saveResult(Long registrationId, Duration finishTime, boolean dnf) {
        Registration registration = registrationService.findById(registrationId);

        Result result = resultRepository.findByRegistrationId(registrationId)
                .orElseGet(() -> Result.builder().registration(registration).build());

        result.setFinishTime(dnf ? null : finishTime);
        result.setDnf(dnf);
        result = resultRepository.save(result);

        recalculatePlaces(registration.getCategory().getId());
        return result;
    }

    /**
     * Przelicza miejsca dla całej kategorii na podstawie kolejności po czasie.
     * Zawodnicy DNF nie otrzymują numeru miejsca ({@code place == null}).
     *
     * @param categoryId identyfikator kategorii
     */
    @Transactional
    public void recalculatePlaces(Long categoryId) {
        List<Result> sorted = findByCategorySorted(categoryId);

        AtomicInteger place = new AtomicInteger(1);
        sorted.forEach(r -> {
            r.setPlace(r.getDnf() ? null : place.getAndIncrement());
            resultRepository.save(r);
        });
    }

    /**
     * Wyszukuje wynik po identyfikatorze.
     *
     * @param id identyfikator wyniku
     * @return znaleziony wynik
     * @throws EntityNotFoundException gdy wynik o podanym id nie istnieje
     */
    public Result findById(Long id) {
        return resultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono wyniku o id: " + id));
    }
}
