package com.orientapp.controller;

import com.orientapp.dto.CategoryFormDto;
import com.orientapp.dto.EventFormDto;
import com.orientapp.dto.ResultFormDto;
import com.orientapp.dto.TrackPointFormDto;
import com.orientapp.dto.TrackPointView;
import com.orientapp.model.*;
import com.orientapp.service.CategoryService;
import com.orientapp.service.EventService;
import com.orientapp.service.RegistrationService;
import com.orientapp.service.ResultService;
import com.orientapp.service.TrackPointService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final RegistrationService registrationService;
    private final ResultService resultService;
    private final TrackPointService trackPointService;

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    // ── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping
    public String dashboard(Model model) {
        var events = eventService.findAll();
        model.addAttribute("totalEvents", events.size());
        model.addAttribute("openEvents", events.stream()
                .filter(e -> e.getStatus() == EventStatus.OPEN).count());
        model.addAttribute("pendingCount", registrationService.countPendingAll());
        model.addAttribute("events", events);
        // Dane do wykresu: liczba zgłoszeń per kategoria dla wszystkich eventów
        var chartData = registrationService.countByCategoryAllEvents();
        model.addAttribute("chartData", chartData);
        model.addAttribute("totalRegistrations",
                chartData.values().stream().mapToLong(Long::longValue).sum());
        return "admin/dashboard";
    }

    // ── CRUD zawodów ─────────────────────────────────────────────────────────

    @GetMapping("/events")
    public String eventList(Model model) {
        model.addAttribute("events", eventService.findAll());
        return "admin/events";
    }

    @GetMapping("/events/new")
    public String newEventForm(Model model) {
        model.addAttribute("eventForm", new EventFormDto());
        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("editMode", false);
        return "admin/event-form";
    }

    @PostMapping("/events")
    public String saveEvent(@Valid @ModelAttribute("eventForm") EventFormDto form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", EventStatus.values());
            model.addAttribute("editMode", false);
            return "admin/event-form";
        }
        Event event = Event.builder()
                .name(form.getName())
                .date(form.getDate())
                .location(form.getLocation())
                .latitude(form.getLatitude())
                .longitude(form.getLongitude())
                .description(form.getDescription())
                .regulations(form.getRegulations())
                .status(form.getStatus())
                .build();
        Event saved = eventService.save(event);
        ra.addFlashAttribute("successMessage", "Zawody \"" + saved.getName() + "\" zostały utworzone.");
        return "redirect:/admin/events/" + saved.getId() + "/edit";
    }

    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id);
        EventFormDto form = new EventFormDto();
        form.setName(event.getName());
        form.setDate(event.getDate());
        form.setLocation(event.getLocation());
        form.setLatitude(event.getLatitude());
        form.setLongitude(event.getLongitude());
        form.setDescription(event.getDescription());
        form.setRegulations(event.getRegulations());
        form.setStatus(event.getStatus());

        model.addAttribute("eventForm", form);
        model.addAttribute("event", event);
        model.addAttribute("categories", categoryService.findByEvent(id));
        model.addAttribute("categoryForm", new CategoryFormDto());
        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("editMode", true);
        return "admin/event-form";
    }

    @PostMapping("/events/{id}")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("eventForm") EventFormDto form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("event", eventService.findById(id));
            model.addAttribute("categories", categoryService.findByEvent(id));
            model.addAttribute("categoryForm", new CategoryFormDto());
            model.addAttribute("statuses", EventStatus.values());
            model.addAttribute("editMode", true);
            return "admin/event-form";
        }
        Event updated = Event.builder()
                .name(form.getName())
                .date(form.getDate())
                .location(form.getLocation())
                .latitude(form.getLatitude())
                .longitude(form.getLongitude())
                .description(form.getDescription())
                .regulations(form.getRegulations())
                .status(form.getStatus())
                .build();
        eventService.update(id, updated);
        ra.addFlashAttribute("successMessage", "Zawody zostały zaktualizowane.");
        return "redirect:/admin/events/" + id + "/edit";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes ra) {
        String name = eventService.findById(id).getName();
        eventService.delete(id);
        ra.addFlashAttribute("successMessage", "Zawody \"" + name + "\" zostały usunięte.");
        return "redirect:/admin/events";
    }

    // ── Kategorie (inline na stronie edycji eventu) ───────────────────────────

    @PostMapping("/events/{id}/categories")
    public String addCategory(@PathVariable Long id,
                              @Valid @ModelAttribute("categoryForm") CategoryFormDto form,
                              BindingResult bindingResult,
                              RedirectAttributes ra) {
        if (!bindingResult.hasErrors()) {
            Category cat = Category.builder()
                    .name(form.getName())
                    .distanceKm(form.getDistanceKm())
                    .build();
            categoryService.save(id, cat);
            ra.addFlashAttribute("successMessage", "Dodano kategorię \"" + form.getName() + "\".");
        }
        return "redirect:/admin/events/" + id + "/edit";
    }

    @PostMapping("/categories/{catId}/delete")
    public String deleteCategory(@PathVariable Long catId,
                                 @RequestParam Long eventId,
                                 RedirectAttributes ra) {
        categoryService.delete(catId);
        ra.addFlashAttribute("successMessage", "Kategoria została usunięta.");
        return "redirect:/admin/events/" + eventId + "/edit";
    }

    // ── Zgłoszenia ────────────────────────────────────────────────────────────

    @GetMapping("/events/{id}/registrations")
    public String registrations(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("registrationsByCategory",
                registrationService.groupByCategory(id));
        return "admin/registrations";
    }

    @PostMapping("/registrations/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam Long eventId,
                          RedirectAttributes ra) {
        registrationService.approve(id);
        ra.addFlashAttribute("successMessage", "Zgłoszenie zatwierdzone.");
        return "redirect:/admin/events/" + eventId + "/registrations";
    }

    @PostMapping("/registrations/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam Long eventId,
                         RedirectAttributes ra) {
        registrationService.reject(id);
        ra.addFlashAttribute("errorMessage", "Zgłoszenie odrzucone.");
        return "redirect:/admin/events/" + eventId + "/registrations";
    }

    // ── Wyniki ────────────────────────────────────────────────────────────────

    @GetMapping("/events/{id}/results")
    public String resultsPanel(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("registrationsByCategory",
                registrationService.groupByCategory(id));
        return "admin/results";
    }

    // ── TrackPoints ───────────────────────────────────────────────────────────

    @GetMapping("/registrations/{registrationId}/track")
    public String trackForm(@PathVariable Long registrationId, Model model) {
        if (!model.containsAttribute("trackPointForm")) {
            model.addAttribute("trackPointForm", new TrackPointFormDto());
        }
        model.addAttribute("registration", registrationService.findById(registrationId));
        List<TrackPointView> trackPoints = trackPointService.findByRegistration(registrationId)
                .stream()
                .map(tp -> new TrackPointView(
                        tp.getId(), tp.getLatitude(), tp.getLongitude(),
                        tp.getTimestamp().toString(), tp.getCheckpointOrder()))
                .toList();
        model.addAttribute("trackPoints", trackPoints);
        model.addAttribute("trackPointsList", trackPointService.findByRegistration(registrationId));
        return "admin/track-form";
    }

    @PostMapping("/registrations/{registrationId}/track")
    public String addTrackPoint(@PathVariable Long registrationId,
                                @Valid @ModelAttribute("trackPointForm") TrackPointFormDto form,
                                BindingResult bindingResult,
                                RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.trackPointForm", bindingResult);
            ra.addFlashAttribute("trackPointForm", form);
            return "redirect:/admin/registrations/" + registrationId + "/track";
        }
        trackPointService.addPoint(registrationId, form.getLatitude(), form.getLongitude(),
                form.getTimestamp(), form.getCheckpointOrder());
        ra.addFlashAttribute("successMessage", "Punkt trasy dodany.");
        return "redirect:/admin/registrations/" + registrationId + "/track";
    }

    @PostMapping("/track-points/{pointId}/delete")
    public String deleteTrackPoint(@PathVariable Long pointId,
                                   @RequestParam Long registrationId,
                                   RedirectAttributes ra) {
        trackPointService.deletePoint(pointId);
        ra.addFlashAttribute("successMessage", "Punkt usunięty.");
        return "redirect:/admin/registrations/" + registrationId + "/track";
    }

    @PostMapping("/registrations/{registrationId}/track/clear")
    public String clearTrack(@PathVariable Long registrationId, RedirectAttributes ra) {
        trackPointService.deleteByRegistration(registrationId);
        ra.addFlashAttribute("successMessage", "Trasa wyczyszczona.");
        return "redirect:/admin/registrations/" + registrationId + "/track";
    }

    @PostMapping("/results")
    public String saveResult(@ModelAttribute ResultFormDto form, RedirectAttributes ra) {
        try {
            Duration duration = null;
            if (!form.isDnf() && form.getFinishTime() != null && !form.getFinishTime().isBlank()) {
                String[] parts = form.getFinishTime().split(":");
                duration = Duration.ofHours(Long.parseLong(parts[0]))
                        .plusMinutes(Long.parseLong(parts[1]))
                        .plusSeconds(parts.length > 2 ? Long.parseLong(parts[2]) : 0);
            }
            resultService.saveResult(form.getRegistrationId(), duration, form.isDnf());
            ra.addFlashAttribute("successMessage", "Wynik zapisany.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Błąd zapisu wyniku: " + e.getMessage());
        }
        return "redirect:/admin/events/" + form.getEventId() + "/results";
    }
}
