# OrientApp — Platforma zarządzania zawodami w biegu na orientację

## Kontekst projektu

Projekt zaliczeniowy na przedmiot "Platformy programistyczne" (PWr, semestr 6).
Aplikacja webowa do zarządzania zawodami w biegu na orientację (orienteering).
**Wersja MVP** — celowo niekompletna, pokazująca progres na zajęciach.

## Stack technologiczny

- **Backend:** Java 21, Spring Boot 3.x
- **Frontend:** Thymeleaf + Bootstrap 5 (SSR, nie SPA)
- **ORM:** Spring Data JPA + Hibernate
- **Baza danych:** PostgreSQL (produkcja) / H2 in-memory (testy)
- **Bezpieczeństwo:** Spring Security (form login, role-based)
- **Zewnętrzne API:** OpenWeatherMap REST API (JSON)
- **Konteneryzacja:** Docker + docker-compose
- **Testy:** JUnit 5 + Mockito
- **Dokumentacja:** Javadoc

## Architektura aplikacji

```
src/main/java/com/orientapp/
├── config/          # SecurityConfig, WebConfig
├── controller/      # MVC Controllers (Admin + Public)
├── model/           # JPA Entities
├── repository/      # Spring Data JPA Repositories
├── service/         # Business logic
├── dto/             # Data Transfer Objects (formularze, API response)
└── exception/       # Custom exceptions + GlobalExceptionHandler
```

## Model danych (encje JPA)

### Event (Zawody)
```java
- Long id
- String name           // np. "SNOB 2026"
- LocalDate date
- String location       // nazwa miejscowości
- Double latitude       // do OpenWeatherMap
- Double longitude
- String description
- EventStatus status    // DRAFT, OPEN, CLOSED
- List<Category> categories
- List<Registration> registrations
```

### Category (Trasa/Kategoria)
```java
- Long id
- String name           // np. "TP12", "TR30"
- Double distanceKm
- Event event
- List<Registration> registrations
```

### AppUser (Użytkownik)
```java
- Long id
- String username       // email
- String passwordHash
- String fullName
- String club           // klub sportowy
- Role role             // ADMIN, COMPETITOR
```

### Registration (Zgłoszenie)
```java
- Long id
- AppUser competitor
- Event event
- Category category
- String chipNumber     // numer chipa SI
- RegistrationStatus status  // PENDING, APPROVED, REJECTED
- LocalDateTime registeredAt
- Result result         // nullable — uzupełniany po zawodach
```

### Result (Wynik)
```java
- Long id
- Registration registration
- Duration finishTime   // czas ukończenia
- Integer place         // wyliczany dynamicznie
- Boolean dnf           // Did Not Finish
```

## Główny flow aplikacji

### Flow publiczny (bez logowania)
1. Strona główna — lista nadchodzących zawodów z pogodą
2. Strona zawodów — opis, kategorie, formularz rejestracji
3. Rejestracja zawodnika — formularz (imię, nazwisko, klub, chip, kategoria)
4. Tabela wyników — publiczna, sortowana per kategoria po czasie

### Flow admina (po zalogowaniu jako ADMIN)
1. Panel admina — dashboard ze statystykami
2. Zarządzanie zawodami — CRUD eventów
3. Lista zgłoszeń — zatwierdzanie / odrzucanie rejestracji (PENDING → APPROVED/REJECTED)
4. Wprowadzanie wyników — formularz z czasem ukończenia per zawodnik
5. Podgląd tabeli wyników z możliwością edycji

## Endpointy (MVC Controllers)

### PublicController (`/`)
- `GET /` — lista zawodów
- `GET /events/{id}` — szczegóły zawodów + formularz rejestracji
- `POST /events/{id}/register` — zapis zgłoszenia (status: PENDING)
- `GET /events/{id}/results` — publiczna tabela wyników

### AdminController (`/admin/**`, wymaga roli ADMIN)
- `GET /admin` — dashboard
- `GET /admin/events` — lista zawodów
- `GET /admin/events/new` — formularz nowego eventu
- `POST /admin/events` — zapis eventu
- `GET /admin/events/{id}/edit` — edycja eventu
- `POST /admin/events/{id}` — aktualizacja eventu
- `DELETE /admin/events/{id}` — usunięcie eventu
- `GET /admin/events/{id}/registrations` — lista zgłoszeń
- `POST /admin/registrations/{id}/approve` — zatwierdzenie
- `POST /admin/registrations/{id}/reject` — odrzucenie
- `GET /admin/events/{id}/results` — panel wyników
- `POST /admin/results` — zapis wyniku

### AuthController
- `GET /login` — strona logowania
- `POST /login` — obsługa Spring Security
- `GET /logout`

## Zewnętrzne API — OpenWeatherMap

**Cel:** Wyświetlanie aktualnej pogody dla lokalizacji zawodów na stronie eventu i w panelu admina.

**Serwis:** `WeatherService`
```java
// GET https://api.openweathermap.org/data/2.5/weather
//     ?lat={lat}&lon={lon}&appid={API_KEY}&units=metric&lang=pl

public WeatherDto getWeather(double lat, double lon);
```

**WeatherDto:**
```java
- String description    // "zachmurzenie małe"
- Double tempCelsius
- Integer humidity
- Double windSpeed
- String iconCode       // do wyświetlenia ikony OWM
```

**Konfiguracja:** klucz API w `application.properties` jako `openweathermap.api.key`,
nigdy nie hardkodować w kodzie.

Obsługa błędów: jeśli API niedostępne, wyświetlić komunikat "Pogoda chwilowo niedostępna" — nie crashować aplikacji.

## Walidacja

Używać `@Valid` + Bean Validation (`jakarta.validation`) na wszystkich DTO/formularzach.

Przykłady:
- `@NotBlank`, `@Size` na polach tekstowych
- `@NotNull`, `@Future` na dacie zawodów
- `@Email` na adresie email
- `@Pattern(regexp = "\\d{6,8}")` na numerze chipa SI

Globalny handler: `@ControllerAdvice` z `@ExceptionHandler` dla:
- `RegistrationClosedException` (zawody nie są OPEN)
- `DuplicateRegistrationException` (zawodnik już zapisany)
- `EntityNotFoundException`
- Błędy walidacji

## Bezpieczeństwo (Spring Security)

```java
// SecurityConfig
http
  .authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/", "/events/**", "/css/**", "/js/**").permitAll()
    .anyRequest().authenticated()
  )
  .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/admin"))
  .logout(logout -> logout.logoutSuccessUrl("/"));
```

Hasła: `BCryptPasswordEncoder`.

Data initializer (`@Component` z `CommandLineRunner`): tworzyć domyślnego admina przy pierwszym uruchomieniu jeśli baza pusta.
```
username: admin@orientapp.pl
password: admin123  (tylko dla dev/demo!)
```

## Frontend (Thymeleaf + Bootstrap 5)

**Styl:** prosty, funkcjonalny Bootstrap 5. Żadnego custom CSS poza drobnymi poprawkami.
Strony mają wyglądać jak solidny projekt studencki — nie jak landing page z drogim designem.

Layouty:
- `layout/base.html` — navbar, footer, importy BS5
- `layout/admin.html` — sidebar admina

Fragmenty:
- `fragments/weather-card.html` — karta pogody
- `fragments/results-table.html` — tabela wyników (reużywalna)

Wykresy: Chart.js (CDN) — prosty słupkowy wykres liczby rejestracji per kategoria na dashboardzie admina. To spełnia wymaganie "graficzna prezentacja danych".

## Kolekcje i filtrowanie (wymaganie zaliczeniowe)

W serwisach używać Java Collections API do:
- Sortowania wyników po czasie ukończenia (`Comparator`)
- Grupowania rejestracji per kategoria (`Collectors.groupingBy`)
- Filtrowania zgłoszeń po statusie (`stream().filter()`)

Nie robić wszystkiego w SQL/JPQL — część logiki celowo w Javie (wymaganie zaliczeniowe).

## Baza danych

**Produkcja:** PostgreSQL 16 (w docker-compose)
**Testy:** H2 in-memory (`application-test.properties`)

Schema: `spring.jpa.hibernate.ddl-auto=update` (dla MVP — nie migracje Flyway).

Przykładowe dane testowe: `DataInitializer.java` z `@Profile("dev")` — tworzy 2 zawody,
4 kategorie, 10 zawodników, kilka wyników.

## Docker

```yaml
# docker-compose.yml
services:
  app:
    build: .
    ports: ["8080:8080"]
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/orientapp
      - OPENWEATHERMAP_API_KEY=${OWM_API_KEY}
    depends_on: [db]
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: orientapp
      POSTGRES_USER: orientapp
      POSTGRES_PASSWORD: orientapp
    volumes: [postgres_data:/var/lib/postgresql/data]
```

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jre
COPY target/orientapp-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Testy jednostkowe (JUnit 5)

Priorytet testowania (MVP):
1. `RegistrationServiceTest` — logika zatwierdzania, duplikaty, walidacja statusu
2. `ResultServiceTest` — obliczanie miejsc, sortowanie po czasie
3. `WeatherServiceTest` — mockowanie HTTP klienta (Mockito), obsługa błędów API

Nie testować kontrolerów ani warstwy JPA w MVP.

## Co jest celowo pominięte w MVP (future work)

- Rejestracja konta dla zawodnika (teraz formularz anonimowy)
- Live tracking GPS
- Eksport wyników do CSV/PDF
- Email potwierdzający rejestrację
- Paginacja list (wystarczy `findAll()` na razie)
- Testy integracyjne
- CI/CD pipeline

## Kolejność implementacji (sugerowana)

1. Setup projektu (Spring Initializr: Web, JPA, Security, Thymeleaf, Validation, PostgreSQL driver)
2. Encje JPA + repozytoria
3. Strony publiczne (lista zawodów, rejestracja, wyniki) — bez logowania
4. Spring Security + panel admina (CRUD eventów)
5. Flow rejestracji (PENDING → APPROVED)
6. Wprowadzanie wyników + tabela
7. Integracja OpenWeatherMap
8. Docker + docker-compose
9. Testy jednostkowe
10. Javadoc na publicznych interfejsach serwisów
