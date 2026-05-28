package com.orientapp.service;

import com.orientapp.model.Category;
import com.orientapp.model.Registration;
import com.orientapp.model.Result;
import com.orientapp.repository.ResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock private ResultRepository resultRepository;
    @Mock private RegistrationService registrationService;
    @Mock private CategoryService categoryService;

    @InjectMocks private ResultService resultService;

    private Result result(long id, Category category, Duration time, boolean dnf) {
        Registration reg = Registration.builder().id(id).category(category).build();
        return Result.builder()
                .id(id)
                .registration(reg)
                .finishTime(dnf ? null : time)
                .dnf(dnf)
                .build();
    }

    @Test
    void recalculatePlaces_assignsPlacesByAscendingTime() {
        Category cat = Category.builder().id(1L).name("TP12").build();
        Result slow   = result(1L, cat, Duration.ofMinutes(40), false);
        Result fast    = result(2L, cat, Duration.ofMinutes(20), false);
        Result medium = result(3L, cat, Duration.ofMinutes(30), false);

        when(resultRepository.findByRegistrationCategoryId(1L))
                .thenReturn(List.of(slow, fast, medium));
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> inv.getArgument(0));

        resultService.recalculatePlaces(1L);

        assertThat(fast.getPlace()).isEqualTo(1);
        assertThat(medium.getPlace()).isEqualTo(2);
        assertThat(slow.getPlace()).isEqualTo(3);
    }

    @Test
    void recalculatePlaces_dnfCompetitorsGetNullPlace() {
        Category cat = Category.builder().id(1L).name("TP12").build();
        Result finisher = result(1L, cat, Duration.ofMinutes(25), false);
        Result dnf      = result(2L, cat, null, true);

        when(resultRepository.findByRegistrationCategoryId(1L))
                .thenReturn(List.of(dnf, finisher));
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> inv.getArgument(0));

        resultService.recalculatePlaces(1L);

        assertThat(finisher.getPlace()).isEqualTo(1);
        assertThat(dnf.getPlace()).isNull();
    }

    @Test
    void findByEventGroupedByCategory_groupsResultsByCategory() {
        Category catA = Category.builder().id(1L).name("TP12").build();
        Category catB = Category.builder().id(2L).name("TR30").build();

        Result a1 = result(1L, catA, Duration.ofMinutes(20), false);
        Result a2 = result(2L, catA, Duration.ofMinutes(30), false);
        Result b1 = result(3L, catB, Duration.ofMinutes(25), false);

        when(resultRepository.findByRegistrationEventId(99L))
                .thenReturn(List.of(a1, a2, b1));

        Map<Category, List<Result>> grouped = resultService.findByEventGroupedByCategory(99L);

        assertThat(grouped).hasSize(2);
        assertThat(grouped.get(catA)).containsExactlyInAnyOrder(a1, a2);
        assertThat(grouped.get(catB)).containsExactly(b1);
    }
}
