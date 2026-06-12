package com.orientapp.config;

import com.orientapp.model.*;
import com.orientapp.repository.*;
import com.orientapp.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Inicjalizator danych demonstracyjnych.
 * <p>
 * Uruchamiany jako {@link CommandLineRunner} przy starcie aplikacji. Gdy baza
 * jest pusta, tworzy konto administratora oraz przykładowy zestaw danych:
 * zawody, kategorie, zawodników, zgłoszenia, wyniki i punkty trasy GPS.
 * Login i hasło administratora można nadpisać zmiennymi środowiskowymi
 * {@code APP_ADMIN_USERNAME} i {@code APP_ADMIN_PASSWORD}.
 */
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
    private final TrackPointRepository trackPointRepository;
    private final PasswordEncoder passwordEncoder;

    /** Login domyślnego administratora; nadpisywalny zmienną środowiskową APP_ADMIN_USERNAME. */
    @Value("${app.admin.username:admin@123}")
    private String adminUsername;

    /** Hasło domyślnego administratora; nadpisywalne zmienną środowiskową APP_ADMIN_PASSWORD. */
    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    // Pętla leśna wokół centrum startowego w Jeleniej Górze (10 punktów kontrolnych)
    private static final double[] BASE_LAT = {
            50.9044, 50.9060, 50.9075, 50.9082, 50.9070,
            50.9048, 50.9020, 50.9008, 50.9015, 50.9044
    };
    private static final double[] BASE_LON = {
            15.7197, 15.7225, 15.7255, 15.7290, 15.7325,
            15.7340, 15.7320, 15.7275, 15.7230, 15.7197
    };
    // Proporcje czasu na każdym odcinku (suma = 1.0)
    private static final double[] LEG_WEIGHTS = {
            0.00, 0.09, 0.11, 0.12, 0.10, 0.11, 0.11, 0.12, 0.13, 0.11
    };

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
        userService.createUser(adminUsername, adminPassword, "Administrator", null, Role.ADMIN);

        // ── Zawody 1 – SNOB 2026 (OPEN, za 2 tygodnie) ───────────────────────
        Event snob = eventRepository.save(Event.builder()
                .name("SNOB 2026")
                .date(LocalDate.now().plusWeeks(2))
                .location("Wrocław")
                .latitude(51.1079)
                .longitude(17.0385)
                .description("Studencki Nocny Bieg na Orientację. Zawody dla studentów wrocławskich uczelni.")
                .maxParticipants(60)
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
                .maxParticipants(120)
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

        // Czas startu zawodów Ligi Wiosennej (3 tygodnie temu, godz. 10:00)
        LocalDateTime ligaStart = liga.getDate().atTime(10, 0, 0);

        // ── Wyniki + TrackPoints Ligi (TR15) ─────────────────────────────────
        Duration t_l1 = Duration.ofMinutes(42).plusSeconds(15);
        Duration t_l3 = Duration.ofMinutes(44).plusSeconds(50);
        Duration t_l5 = Duration.ofMinutes(47).plusSeconds(3);
        Duration t_l7 = Duration.ofMinutes(38).plusSeconds(40);
        Duration t_l9 = Duration.ofMinutes(28); // DNF – dotarł do 6. punktu

        result(l1, t_l1, false);
        result(l3, t_l3, false);
        result(l5, t_l5, false);
        result(l7, t_l7, false);
        result(l9, null, true);

        track(l1, ligaStart, t_l1, false,  0.0002, -0.0001);
        track(l3, ligaStart, t_l3, false, -0.0001,  0.0003);
        track(l5, ligaStart, t_l5, false,  0.0003,  0.0001);
        track(l7, ligaStart, t_l7, false, -0.0002, -0.0002);
        track(l9, ligaStart, t_l9, true,   0.0001,  0.0002); // DNF – 6 punktów

        // ── Wyniki + TrackPoints Ligi (TR30) ─────────────────────────────────
        Duration t_l2  = Duration.ofHours(1).plusMinutes(28).plusSeconds(5);
        Duration t_l4  = Duration.ofHours(1).plusMinutes(35).plusSeconds(22);
        Duration t_l6  = Duration.ofHours(1).plusMinutes(21).plusSeconds(44);
        Duration t_l8  = Duration.ofHours(1).plusMinutes(42).plusSeconds(11);
        Duration t_l10 = Duration.ofMinutes(55); // DNF – dotarł do 6. punktu

        result(l2, t_l2,  false);
        result(l4, t_l4,  false);
        result(l6, t_l6,  false);
        result(l8, t_l8,  false);
        result(l10, null, true);

        track(l2,  ligaStart, t_l2,  false,  0.0002,  0.0002);
        track(l4,  ligaStart, t_l4,  false, -0.0003, -0.0001);
        track(l6,  ligaStart, t_l6,  false,  0.0001, -0.0003);
        track(l8,  ligaStart, t_l8,  false, -0.0001,  0.0003);
        track(l10, ligaStart, t_l10, true,   0.0003, -0.0002); // DNF – 6 punktów

        log.info("Demo data seeded: 2 events, 4 categories, 10 competitors, 15 registrations, 8 results, 95 track points.");
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

    /**
     * Generuje 10 (lub 6 dla DNF) punktów trasy zawodnika.
     * @param latOff  stałe odchylenie szerokości od trasy wzorcowej (identyfikuje zawodnika)
     * @param lonOff  stałe odchylenie długości od trasy wzorcowej
     */
    private void track(Registration reg, LocalDateTime raceStart,
                       Duration totalTime, boolean dnf,
                       double latOff, double lonOff) {
        int numPoints = dnf ? 6 : 10;
        long totalSeconds = totalTime.toSeconds();

        // Małe szumy per-punkt (deterministyczne): ±0.00005 stopnia ≈ ±5m
        double[] microLat = {0.00002, -0.00004, 0.00003, -0.00001,  0.00005,
                            -0.00003,  0.00001,  0.00004, -0.00002,  0.00000};
        double[] microLon = {-0.00003, 0.00002, -0.00005, 0.00004,  -0.00001,
                              0.00003, -0.00004,  0.00001,  0.00005, -0.00002};

        LocalDateTime current = raceStart;
        for (int i = 0; i < numPoints; i++) {
            if (i > 0) {
                // Czas odcinka proporcjonalny do wagi + deterministyczna zmiana ±8%
                double factor = 1.0 + ((i % 3) - 1) * 0.08;
                long legSeconds = (long)(totalSeconds * LEG_WEIGHTS[i] * factor);
                current = current.plusSeconds(Math.max(legSeconds, 60));
            }
            trackPointRepository.save(TrackPoint.builder()
                    .registration(reg)
                    .latitude(BASE_LAT[i] + latOff + microLat[i])
                    .longitude(BASE_LON[i] + lonOff + microLon[i])
                    .timestamp(current)
                    .checkpointOrder(i + 1)
                    .build());
        }
    }
}
