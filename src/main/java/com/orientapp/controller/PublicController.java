package com.orientapp.controller;

import com.orientapp.dto.RegistrationFormDto;
import com.orientapp.dto.TrackPointView;
import com.orientapp.exception.RegistrationClosedException;
import com.orientapp.model.Event;
import com.orientapp.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Kontroler części publicznej aplikacji (dostępnej bez logowania).
 * <p>
 * Obsługuje stronę główną z listą zawodów, szczegóły zawodów, anonimową
 * rejestrację zawodników, regulamin, publiczną tabelę wyników oraz podgląd
 * trasy GPS pojedynczego zawodnika na mapie.
 */
@Controller
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final RegistrationService registrationService;
    private final ResultService resultService;
    private final TrackPointService trackPointService;

    /**
     * Strona główna — wyświetla zawody nadchodzące i zakończone.
     *
     * @param model model widoku (atrybuty {@code upcomingEvents}, {@code pastEvents})
     * @return nazwa widoku listy zawodów
     */
    @GetMapping("/")
    public String home(Model model) {
        var all = eventService.findUpcoming();
        var today = java.time.LocalDate.now();
        model.addAttribute("upcomingEvents", all.stream()
                .filter(e -> !e.getDate().isBefore(today) && e.getStatus() != com.orientapp.model.EventStatus.CLOSED)
                .toList());
        model.addAttribute("pastEvents", all.stream()
                .filter(e -> e.getDate().isBefore(today) || e.getStatus() == com.orientapp.model.EventStatus.CLOSED)
                .toList());
        return "public/events";
    }

    /**
     * Szczegóły zawodów wraz z listą kategorii i liczbą zapisanych zawodników.
     *
     * @param id    identyfikator zawodów
     * @param model model widoku (atrybuty {@code event}, {@code categories}, {@code participantCount})
     * @return nazwa widoku szczegółów zawodów
     */
    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("categories", categoryService.findByEvent(id));
        model.addAttribute("participantCount", registrationService.findByEvent(id).size());
        return "public/event-detail";
    }

    /**
     * Wyświetla formularz rejestracji zawodnika na dane zawody.
     *
     * @param id    identyfikator zawodów
     * @param model model widoku (atrybut {@code registrationForm})
     * @return nazwa widoku formularza rejestracji
     */
    @GetMapping("/events/{id}/register")
    public String registerForm(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id);
        model.addAttribute("event", event);
        model.addAttribute("categories", categoryService.findByEvent(id));
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationFormDto());
        }
        return "public/register";
    }

    /**
     * Przyjmuje zgłoszenie rejestracyjne (status PENDING).
     * <p>
     * Przy błędach walidacji ponownie wyświetla formularz; przy zamkniętej
     * rejestracji zwraca komunikat flash i przekierowuje na stronę zawodów.
     *
     * @param id                 identyfikator zawodów
     * @param form               dane formularza rejestracji
     * @param bindingResult      wynik walidacji formularza
     * @param redirectAttributes atrybuty przekierowania (komunikaty flash)
     * @param model              model widoku (używany przy błędach walidacji)
     * @return widok formularza (przy błędach) lub przekierowanie na stronę zawodów
     */
    @PostMapping("/events/{id}/register")
    public String register(@PathVariable Long id,
                           @Valid @ModelAttribute("registrationForm") RegistrationFormDto form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("event", eventService.findById(id));
            model.addAttribute("categories", categoryService.findByEvent(id));
            return "public/register";
        }
        try {
            registrationService.registerAnonymous(
                    id, form.getCategoryId(),
                    form.getFirstName(), form.getLastName(),
                    form.getEmail(), form.getPhone(), form.getDateOfBirth(),
                    form.getClub(), form.getChipNumber());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Zgłoszenie przyjęte! Oczekuje na zatwierdzenie przez organizatora.");
        } catch (RegistrationClosedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + id;
    }

    /**
     * Wyświetla regulamin zawodów.
     *
     * @param id    identyfikator zawodów
     * @param model model widoku
     * @return nazwa widoku regulaminu
     */
    @GetMapping("/events/{id}/regulations")
    public String regulations(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        return "public/regulations";
    }

    /**
     * Publiczna tabela wyników zawodów, pogrupowana po kategoriach.
     *
     * @param id    identyfikator zawodów
     * @param model model widoku (atrybut {@code resultsByCategory})
     * @return nazwa widoku tabeli wyników
     */
    @GetMapping("/events/{id}/results")
    public String results(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("resultsByCategory", resultService.findByEventGroupedByCategory(id));
        return "public/results";
    }

    /**
     * Podgląd trasy GPS pojedynczego zawodnika na mapie.
     *
     * @param eventId        identyfikator zawodów
     * @param registrationId identyfikator zgłoszenia zawodnika
     * @param model          model widoku (atrybut {@code trackPoints})
     * @return nazwa widoku mapy z trasą
     */
    @GetMapping("/events/{eventId}/results/{registrationId}")
    public String trackMap(@PathVariable Long eventId,
                           @PathVariable Long registrationId,
                           Model model) {
        model.addAttribute("event", eventService.findById(eventId));
        model.addAttribute("registration", registrationService.findById(registrationId));
        List<TrackPointView> trackPoints = trackPointService.findByRegistration(registrationId)
                .stream()
                .map(tp -> new TrackPointView(
                        tp.getId(), tp.getLatitude(), tp.getLongitude(),
                        tp.getTimestamp().toString(), tp.getCheckpointOrder()))
                .toList();
        model.addAttribute("trackPoints", trackPoints);
        return "public/track-map";
    }
}
