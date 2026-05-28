package com.orientapp.service;

import com.orientapp.exception.DuplicateRegistrationException;
import com.orientapp.exception.RegistrationClosedException;
import com.orientapp.model.*;
import com.orientapp.repository.RegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventService eventService;
    @Mock private CategoryService categoryService;
    @Mock private AppUserService userService;

    @InjectMocks private RegistrationService registrationService;

    private Event eventWithStatus(EventStatus status) {
        return Event.builder().id(1L).name("SNOB 2026").status(status).build();
    }

    @Test
    void register_onOpenEvent_savesWithPendingStatus() {
        Event event = eventWithStatus(EventStatus.OPEN);
        Category category = Category.builder().id(10L).name("TP12").build();
        AppUser competitor = AppUser.builder().id(20L).fullName("Jan Kowalski").build();

        when(eventService.findById(1L)).thenReturn(event);
        when(registrationRepository.existsByCompetitorIdAndEventId(20L, 1L)).thenReturn(false);
        when(categoryService.findById(10L)).thenReturn(category);
        when(userService.findById(20L)).thenReturn(competitor);
        when(registrationRepository.save(any(Registration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Registration result = registrationService.register(1L, 10L, 20L, "123456");

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.PENDING);
        assertThat(result.getEvent()).isEqualTo(event);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getCompetitor()).isEqualTo(competitor);
        assertThat(result.getChipNumber()).isEqualTo("123456");
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    void register_onClosedEvent_throwsRegistrationClosed() {
        when(eventService.findById(1L)).thenReturn(eventWithStatus(EventStatus.CLOSED));

        assertThatThrownBy(() -> registrationService.register(1L, 10L, 20L, "123456"))
                .isInstanceOf(RegistrationClosedException.class);

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void register_whenCompetitorAlreadyRegistered_throwsDuplicate() {
        Event event = eventWithStatus(EventStatus.OPEN);
        when(eventService.findById(1L)).thenReturn(event);
        when(registrationRepository.existsByCompetitorIdAndEventId(20L, 1L)).thenReturn(true);
        when(userService.findById(20L))
                .thenReturn(AppUser.builder().id(20L).fullName("Jan Kowalski").build());

        assertThatThrownBy(() -> registrationService.register(1L, 10L, 20L, "123456"))
                .isInstanceOf(DuplicateRegistrationException.class);

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void approve_changesStatusFromPendingToApproved() {
        Registration reg = Registration.builder()
                .id(5L).status(RegistrationStatus.PENDING).build();
        when(registrationRepository.findById(5L)).thenReturn(java.util.Optional.of(reg));
        when(registrationRepository.save(any(Registration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Registration result = registrationService.approve(5L);

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.APPROVED);
        verify(registrationRepository).save(reg);
    }

    @Test
    void reject_changesStatusFromPendingToRejected() {
        Registration reg = Registration.builder()
                .id(5L).status(RegistrationStatus.PENDING).build();
        when(registrationRepository.findById(5L)).thenReturn(java.util.Optional.of(reg));
        when(registrationRepository.save(any(Registration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Registration result = registrationService.reject(5L);

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.REJECTED);
        verify(registrationRepository).save(reg);
    }
}
