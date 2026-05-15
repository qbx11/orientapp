package com.orientapp.config;

import com.orientapp.model.*;
import com.orientapp.repository.*;
import com.orientapp.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final AppUserService userService;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RegistrationRepository registrationRepository;
    private final ResultRepository resultRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedAll();
        }
    }

    private void seedAll() {
        log.info("Seeding demo data...");

        // ── Admin ─────────────────────────────────────────────────────────────
        userService.createUser("admin@123", "admin123", "Administrator", null, Role.ADMIN);

        // ── Zawody 1 – SNOB 2026 (OPEN, za 2 tygodnie) ───────────────────────
        Event snob = eventRepository.save(Event.builder()
                .name("SNOB 2026")
                .date(LocalDate.now().plusWeeks(2))
                .location("Wrocław")
                .latitude(51.1079)
                .longitude(17.0385)
                .description("Studencki Nocny Bieg na Orientację. Zawody dla studentów wrocławskich uczelni.")
                .status(EventStatus.OPEN)
                .build());

        Category snobTp12 = categoryRepository.save(Category.builder().name("TP12").distanceKm(4.2).event(snob).build());
        Category snobTp20 = categoryRepository.save(Category.builder().name("TP20").distanceKm(7.5).event(snob).build());

        // ── Zawody 2 – Liga Wiosenna (CLOSED, wyniki gotowe) ─────────────────
        Event liga = eventRepository.save(Event.builder()
                .name("Liga Wiosenna 2026 – Runda 1")
                .date(LocalDate.now().minusWeeks(3))
                .location("Jelenia Góra")
                .latitude(50.9044)
                .longitude(15.7197)
                .description("Pierwsza runda Ligi Wiosennej. Trasy przez Karkonosze.")
                .status(EventStatus.CLOSED)
                .build());

        Category ligaTr15 = categoryRepository.save(Category.builder().name("TR15").distanceKm(5.0).event(liga).build());
        Category ligaTr30 = categoryRepository.save(Category.builder().name("TR30").distanceKm(10.0).event(liga).build());

        // ── Zawodnicy ─────────────────────────────────────────────────────────
        List<AppUser> competitors = List.of(
                makeCompetitor("Anna Kowalska",    "KS Azymut Wrocław"),
                makeCompetitor("Piotr Nowak",      "UKS Kompas Jelenia Góra"),
                makeCompetitor("Marta Wiśniewska", "AZS Politechnika"),
                makeCompetitor("Krzysztof Zając",  "KS Azymut Wrocław"),
                makeCompetitor("Zofia Kamińska",   "UKS Kompas Jelenia Góra"),
                makeCompetitor("Tomasz Lewandowski","Bez klubu"),
                makeCompetitor("Ewa Dąbrowska",    "AZS Politechnika"),
                makeCompetitor("Marcin Wójcik",    "KS Azymut Wrocław"),
                makeCompetitor("Karolina Kubiak",  "UKS Kompas Jelenia Góra"),
                makeCompetitor("Rafał Szymański",  "Bez klubu")
        );

        // ── Rejestracje na SNOB (OPEN – oczekujące i zatwierdzone) ───────────
        Registration s1 = reg(competitors.get(0), snob, snobTp12, "100001", RegistrationStatus.APPROVED);
        Registration s2 = reg(competitors.get(1), snob, snobTp20, "100002", RegistrationStatus.APPROVED);
        Registration s3 = reg(competitors.get(2), snob, snobTp12, "100003", RegistrationStatus.PENDING);
        Registration s4 = reg(competitors.get(3), snob, snobTp20, "100004", RegistrationStatus.PENDING);
        Registration s5 = reg(competitors.get(4), snob, snobTp12, "100005", RegistrationStatus.APPROVED);

        // ── Rejestracje na Ligę (CLOSED – z wynikami) ────────────────────────
        Registration l1 = reg(competitors.get(0), liga, ligaTr15, "100001", RegistrationStatus.APPROVED);
        Registration l2 = reg(competitors.get(1), liga, ligaTr30, "100002", RegistrationStatus.APPROVED);
        Registration l3 = reg(competitors.get(2), liga, ligaTr15, "100003", RegistrationStatus.APPROVED);
        Registration l4 = reg(competitors.get(3), liga, ligaTr30, "100004", RegistrationStatus.APPROVED);
        Registration l5 = reg(competitors.get(4), liga, ligaTr15, "100005", RegistrationStatus.APPROVED);
        Registration l6 = reg(competitors.get(5), liga, ligaTr30, "100006", RegistrationStatus.APPROVED);
        Registration l7 = reg(competitors.get(6), liga, ligaTr15, "100007", RegistrationStatus.APPROVED);
        Registration l8 = reg(competitors.get(7), liga, ligaTr30, "100008", RegistrationStatus.APPROVED);
        Registration l9 = reg(competitors.get(8), liga, ligaTr15, "100009", RegistrationStatus.APPROVED);
        Registration l10= reg(competitors.get(9), liga, ligaTr30, "100010", RegistrationStatus.APPROVED);

        // ── Wyniki Ligi (TR15) ────────────────────────────────────────────────
        result(l1, Duration.ofMinutes(42).plusSeconds(15), false);
        result(l3, Duration.ofMinutes(44).plusSeconds(50), false);
        result(l5, Duration.ofMinutes(47).plusSeconds(3),  false);
        result(l7, Duration.ofMinutes(38).plusSeconds(40), false);
        result(l9, null, true);  // DNF

        // ── Wyniki Ligi (TR30) ────────────────────────────────────────────────
        result(l2, Duration.ofHours(1).plusMinutes(28).plusSeconds(5),  false);
        result(l4, Duration.ofHours(1).plusMinutes(35).plusSeconds(22), false);
        result(l6, Duration.ofHours(1).plusMinutes(21).plusSeconds(44), false);
        result(l8, Duration.ofHours(1).plusMinutes(42).plusSeconds(11), false);
        result(l10, null, true);  // DNF

        log.info("Demo data seeded: 2 events, 4 categories, 10 competitors, 15 registrations, 8 results.");
    }

    private AppUser makeCompetitor(String fullName, String club) {
        String username = fullName.toLowerCase()
                .replace(" ", ".")
                .replace("ą","a").replace("ć","c").replace("ę","e")
                .replace("ł","l").replace("ń","n").replace("ó","o")
                .replace("ś","s").replace("ź","z").replace("ż","z")
                + "@demo.orientapp.pl";
        return userRepository.save(AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode("demo123"))
                .fullName(fullName)
                .club(club)
                .role(Role.COMPETITOR)
                .build());
    }

    private Registration reg(AppUser competitor, Event event, Category category,
                              String chip, RegistrationStatus status) {
        return registrationRepository.save(Registration.builder()
                .competitor(competitor)
                .event(event)
                .category(category)
                .chipNumber(chip)
                .status(status)
                .registeredAt(LocalDateTime.now().minusDays((long)(Math.random() * 14 + 1)))
                .build());
    }

    private void result(Registration registration, Duration finishTime, boolean dnf) {
        resultRepository.save(Result.builder()
                .registration(registration)
                .finishTime(finishTime)
                .dnf(dnf)
                .build());
    }
}
