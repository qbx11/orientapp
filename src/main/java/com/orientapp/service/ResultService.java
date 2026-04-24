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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultService {

    private final ResultRepository resultRepository;
    private final RegistrationService registrationService;
    private final CategoryService categoryService;

    /**
     * Zwraca wyniki dla kategorii posortowane rosnąco po czasie ukończenia.
     * Zawodnicy DNF trafiają na koniec listy.
     * Sortowanie w Javie przez Comparator.
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
     * Grupuje wyniki eventu per kategoria — Collectors.groupingBy w Javie.
     * Każda lista jest posortowana po czasie (DNF na końcu).
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
     * Zapisuje wynik dla zgłoszenia i przelicza miejsca w kategorii.
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
     * Przelicza miejsca dla całej kategorii.
     * Miejsca przyznawane przez Comparator, DNF nie dostają numeru miejsca.
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

    public Result findById(Long id) {
        return resultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono wyniku o id: " + id));
    }
}
