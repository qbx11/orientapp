package com.orientapp.service;

import com.orientapp.exception.EntityNotFoundException;
import com.orientapp.model.Category;
import com.orientapp.model.Event;
import com.orientapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis zarządzania kategoriami (trasami) zawodów ({@link Category}).
 * <p>
 * Każda kategoria należy do jednego {@link Event}. Serwis udostępnia operacje
 * CRUD oraz pobieranie kategorii danego eventu posortowanych po nazwie.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventService eventService;

    /**
     * Zwraca kategorie danego eventu posortowane rosnąco po nazwie.
     * Sortowanie odbywa się w Javie ({@link Comparator}).
     *
     * @param eventId identyfikator zawodów
     * @return lista kategorii (może być pusta)
     */
    public List<Category> findByEvent(Long eventId) {
        return categoryRepository.findByEventId(eventId).stream()
                .sorted(Comparator.comparing(Category::getName))
                .collect(Collectors.toList());
    }

    /**
     * Wyszukuje kategorię po identyfikatorze.
     *
     * @param id identyfikator kategorii
     * @return znaleziona kategoria
     * @throws EntityNotFoundException gdy kategoria o podanym id nie istnieje
     */
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kategorii o id: " + id));
    }

    /**
     * Zapisuje kategorię i przypina ją do wskazanego eventu.
     *
     * @param eventId  identyfikator zawodów, do których należy kategoria
     * @param category kategoria do zapisania
     * @return zapisana kategoria
     * @throws EntityNotFoundException gdy event o podanym id nie istnieje
     */
    @Transactional
    public Category save(Long eventId, Category category) {
        Event event = eventService.findById(eventId);
        category.setEvent(event);
        return categoryRepository.save(category);
    }

    /**
     * Usuwa kategorię o podanym identyfikatorze.
     *
     * @param id identyfikator kategorii
     * @throws EntityNotFoundException gdy kategoria o podanym id nie istnieje
     */
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Nie znaleziono kategorii o id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
