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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventService eventService;

    public List<Category> findByEvent(Long eventId) {
        return categoryRepository.findByEventId(eventId).stream()
                .sorted(Comparator.comparing(Category::getName))
                .collect(Collectors.toList());
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kategorii o id: " + id));
    }

    @Transactional
    public Category save(Long eventId, Category category) {
        Event event = eventService.findById(eventId);
        category.setEvent(event);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Nie znaleziono kategorii o id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
