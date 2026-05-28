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

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final RegistrationService registrationService;
    private final ResultService resultService;
    private final TrackPointService trackPointService;

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

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("categories", categoryService.findByEvent(id));
        return "public/event-detail";
    }

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
                    form.getClub(), form.getChipNumber());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Zgłoszenie przyjęte! Oczekuje na zatwierdzenie przez organizatora.");
        } catch (RegistrationClosedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + id;
    }

    @GetMapping("/events/{id}/regulations")
    public String regulations(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        return "public/regulations";
    }

    @GetMapping("/events/{id}/results")
    public String results(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("resultsByCategory", resultService.findByEventGroupedByCategory(id));
        return "public/results";
    }

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
