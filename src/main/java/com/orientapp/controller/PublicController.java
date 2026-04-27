package com.orientapp.controller;

import com.orientapp.dto.RegistrationFormDto;
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

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final RegistrationService registrationService;
    private final ResultService resultService;
    private final WeatherService weatherService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("events", eventService.findUpcoming());
        return "public/events";
    }

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id);
        model.addAttribute("event", event);
        model.addAttribute("categories", categoryService.findByEvent(id));
        model.addAttribute("weather",
                weatherService.getWeather(event.getLatitude(), event.getLongitude()).orElse(null));
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationFormDto());
        }
        return "public/event-detail";
    }

    @PostMapping("/events/{id}/register")
    public String register(@PathVariable Long id,
                           @Valid @ModelAttribute("registrationForm") RegistrationFormDto form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            Event event = eventService.findById(id);
            model.addAttribute("event", event);
            model.addAttribute("categories", categoryService.findByEvent(id));
            model.addAttribute("weather",
                    weatherService.getWeather(event.getLatitude(), event.getLongitude()).orElse(null));
            return "public/event-detail";
        }
        try {
            registrationService.registerAnonymous(
                    id, form.getCategoryId(),
                    form.getFullName(), form.getClub(), form.getChipNumber());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Zgłoszenie przyjęte! Oczekuje na zatwierdzenie przez organizatora.");
        } catch (RegistrationClosedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + id;
    }

    @GetMapping("/events/{id}/results")
    public String results(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("resultsByCategory", resultService.findByEventGroupedByCategory(id));
        return "public/results";
    }
}
