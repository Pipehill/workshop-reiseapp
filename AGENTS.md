# Prosjektkontekst og instruksjoner

Denne filen er felles prosjektkontekst for KI-kodeassistenter og utviklere.
Instruksjonene gjelder hele repoet. Hold filen oppdatert når prosjektet får nye,
varige beslutninger. Verktøy som ikke automatisk leser AGENTS.md, skal henvises
til denne filen gjennom sin egen konfigurasjon.

## Formål

Vi bygger en backend til en fiktiv reiseapp for opplæring og workshops.
Deltakerne skal integrere mot API-ene og lære om API-integrasjon.

Som standard skal kode og API-er lages etter beste evne, med god kodekvalitet
og godt API-design. Når ikke annet er spesifisert, skal løsningene være
lesbare, stabile, testbare og enkle å integrere mot.

Suboptimale løsninger skal bare lages når brukeren uttrykkelig ber om det.
Dette kan være API-er som ikke er utformet med en brukerflate i tankene,
eller som bevisst er krevende å integrere mot. Begrens slike avvik til det
som er bestilt, og dokumenter tilsiktet oppførsel og læringsformålet.
Bevar uttrykkelig bestilte integrasjonsutfordringer med mindre oppgaven
innebærer å endre dem. Utilsiktede feil skal fortsatt rettes.

## Fastlagte tekniske føringer

- Koden skrives i Kotlin.
- Backendrammeverket er Spring Boot.
- Prosjektet bygges med Maven.
- Maven skal installeres lokalt ved behov for bygg, og kjøres med `mvn`.
  Ikke legg til Maven Wrapper.
- API-ene utvikles spec first med OpenAPI.
- Endepunkter og datamodeller spesifiseres i OpenAPI før de implementeres.
- API-kode genereres fra spesifikasjonen som del av Maven-bygget.
- Koden skal ligge på GitHub.
- GitHub Actions skal bygge og teste koden for hver pull request.
- Eksterne GitHub Actions og gjenbrukbare workflows skal låses til full
  commit-SHA (40 heksadesimale tegn), ikke branch eller tag, for å redusere
  risikoen for supply chain-angrep gjennom flyttede referanser. Oppgi versjonen
  i en kommentar ved `uses`. Verifiser SHA mot det offisielle repositoryet ved
  innføring og oppdatering; oppdater SHA og versjonskommentar sammen.

## Versjoner av rammeverk og avhengigheter

- Bruk nyeste stabile versjoner av rammeverk, avhengigheter og verktøy med
  mindre annet er uttrykkelig spesifisert.
- Dersom en komponent tilbyr LTS (Long-Term Support), foretrekk nyeste
  støttede LTS-versjon med siste tilgjengelige patch fremfor nyere versjoner
  uten LTS.
- Verifiser tilgjengelige versjoner og kompatibilitet mot offisielle kilder
  når versjoner velges eller oppdateres. Dokumenter eventuelle avvik som
  er nødvendige av hensyn til kompatibilitet.
- Lås valgte versjoner i bygg- og containeroppsettet for repeterbare bygg;
  ikke bruk flytende `latest`-referanser.

### Valgte versjoner

De opprinnelige versjonsvalgene ble kontrollert mot offisielle kilder
13. september 2026; Flyway og PostgreSQL JDBC ble kontrollert 15. september 2026.
Kodegenerering, kompilering og pakking av kontraktmodulen er verifisert på
Windows med Java 25. Container- og Compose-kjøring er verifisert med Podman
6.0.2 (Windows/WSL2, rootless) og Docker Compose 5.5.1 som provider, men ikke
med Docker Engine.

| Teknologi | Valgt versjon | Begrunnelse og kilde |
| --- | --- | --- |
| JDK (Eclipse Temurin) | 25.0.4.1+1, Java 25 LTS | Nyeste patch i valgt LTS-linje. [Utgivelse](https://github.com/adoptium/temurin25-binaries/releases/tag/jdk-25.0.4.1+1) og [LTS-støtte](https://adoptium.net/support/). |
| Kotlin | 2.4.20 | Nyeste stabile utgivelse. Bruk samme versjon for kompilator, Maven-plugin og Kotlin-biblioteker. [Utgivelser](https://kotlinlang.org/docs/releases.html). |
| Spring Boot | 4.1.1 | Nyeste stabile utgivelse; ingen kommersiell støtteavtale forutsettes. [Utgivelse](https://github.com/spring-projects/spring-boot/releases/tag/v4.1.1). |
| Maven | 3.9.16 | Nyeste stabile utgivelse; 3.10 og 4.0 er foreløpig forhåndsversjoner. [Nedlasting](https://maven.apache.org/download.cgi). |
| PostgreSQL | 18.6 | Nyeste stabile hovedversjon med siste vedlikeholdsutgivelse. PostgreSQL gir fem års støtte per hovedversjon, uten egen LTS-linje. [Versjonspolicy](https://www.postgresql.org/support/versioning/). |
| Flyway | 12.4.0 | Versjonen forvaltes av Spring Boot 4.1.1. PostgreSQL 18 er støttet, og `flyway-database-postgresql` brukes som separat databasemodul. [Spring Boot dependency management](https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html) og [Flyway PostgreSQL-støtte](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database). |
| PostgreSQL JDBC | 42.7.13 | Nyeste stabile JDBC-driver, forvaltet av Spring Boot 4.1.1. [pgJDBC-nedlasting](https://jdbc.postgresql.org/download/). |
| Spring Data JPA | 4.1.1 | Versjonen forvaltes av Spring Boot 4.1.1. Repositoryspørringer deklareres med JPQL og `@Query`. [Query methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html). |
| OpenAPI-spesifikasjon | 3.0.4 | Bevisst kompatibilitetsunntak for kodegenerering, se nedenfor. [Spesifikasjon](https://spec.openapis.org/oas/v3.0.4.html). |
| OpenAPI Generator Maven Plugin | 7.25.0 | Nyeste stabile utgivelse. Velg generatoren `kotlin-spring`. [Utgivelse](https://github.com/OpenAPITools/openapi-generator/releases/tag/v7.25.0). |
| Podman | 6.1.1 | Foretrukket referanseversjon for lokal kjøring. [Utgivelse](https://github.com/podman-container-tools/podman/releases/tag/v6.1.1). |
| Docker Engine | 29.8.0 | Alternativ referanseversjon for lokal kjøring. Dette er Engine-versjonen, ikke Docker Desktop-versjonen. [Utgivelsesnotater](https://docs.docker.com/engine/release-notes/29/). |

Spring Boot 4.1.1 støtter Java 25 og krever minst Kotlin 2.2.x.
Se [systemkrav](https://docs.spring.io/spring-boot/system-requirements.html)
og [Kotlin-støtte](https://docs.spring.io/spring-boot/reference/features/kotlin.html).
Bruk Spring Boots dependency management for avhengigheter den forvalter,
med Kotlin-versjonen over som eksplisitt valg.

OpenAPI 3.2.0 er nyeste spesifikasjon, men OpenAPI Generator oppgir støtte
for 3.0 og beta-støtte for 3.1, uten å oppgi støtte for 3.2. Derfor velges
3.0.4 for dette prosjektet. Se [spesifikasjonsversjoner](https://spec.openapis.org/oas/)
og [generatorens kompatibilitet](https://github.com/OpenAPITools/openapi-generator/tree/v7.25.0#11---compatibility).
Konfigurer `kotlin-spring` for Spring Boot 4 med `useSpringBoot4=true`;
se [generatorens dokumentasjon](https://openapi-generator.tech/docs/generators/kotlin-spring/).

Podman og Docker-versjonene er referanser for implementasjon og testing,
ikke et krav om at deltakere har nøyaktig samme patchversjon. Dokumenter
faktisk verifisert kompatibilitet når containeroppsettet er på plass.

GitHub og GitHub Actions brukes som tjenester og har ingen prosjektstyrt
produktversjon. CI bruker `actions/checkout` v7.0.1 og
`actions/setup-java` v6.0.1, begge låst til full commit-SHA i workflowen.
SHA-ene er verifisert mot taggene i de offisielle repositoryene med
`git ls-remote`. Versjonene er kontrollert mot de offisielle utgivelsene
17. september 2026: [checkout](https://github.com/actions/checkout/releases/tag/v7.0.1)
og [setup-java](https://github.com/actions/setup-java/releases/tag/v6.0.1).

## Autentisering og autorisering

Autentisering og autorisering er ikke prioritert i første omgang.
Ingen endepunkter trenger slik beskyttelse. Ikke innfør krav om innlogging,
API-nøkler eller tilgangskontroll med mindre det blir uttrykkelig bestilt.

## Lagring og startdata

- Data skal lagres i PostgreSQL, i første omgang lokalt på maskinen som
  kjører systemet.
- Når PostgreSQL kjører i en container, skal data lagres i et persistent
  volum slik at de overlever restart og gjenoppretting av containeren.
- Fornuftige, fiktive startdata skal seedes ved oppstart når databasen
  initialiseres.
- Data som opprettes eller endres under kjøring, skal overleve en restart.
  Seeding ved senere oppstarter skal ikke overskrive eksisterende data,
  gjenopprette slettede data eller opprette duplikater.
- Det skal være enkelt å resette databasen til en kjent starttilstand med
  de opprinnelige startdataene. Reset skal være en eksplisitt handling,
  adskilt fra vanlig oppstart, og fremgangsmåten skal dokumenteres.

## Containere og lokal kjøring

- PostgreSQL og API-et skal kunne kjøres i hver sin container.
- Podman er det foretrukne verktøyet for lokal kjøring.
- Docker skal også støttes, slik at deltakere som allerede har Docker
  installert, kan kjøre systemet uten å installere Podman.
- Etter bygging skal alle nødvendige tjenester kunne startes med enten
  Podman eller Docker. Containerbilder og oppsett skal fungere med begge.
- Unngå avhengigheter til funksjoner som bare finnes i ett av verktøyene.
  Dokumenter eventuelle forskjeller i forutsetninger og kommandoer.
- Dokumenter og verifiser oppstart, nedstenging og reset av databasen for
  begge verktøy når containeroppsettet implementeres. Vanlig nedstenging
  skal bevare databasevolumet.

## Arbeidsflyt for API-endringer

OpenAPI-spesifikasjonen skal være på engelsk, inkludert beskrivelser,
oppsummeringer og eksempelverdier. Eksisterende API-identifikatorer bevares.

1. Beskriv eller oppdater kontrakten i OpenAPI-spesifikasjonen først.
2. Generer API-koden gjennom Maven. Ikke rediger genererte filer manuelt.
3. Implementer oppførselen i håndskrevet Kotlin-kode adskilt fra generert kode.
4. Legg til relevante tester for kontrakten og oppførselen, inkludert tilsiktede
   integrasjonsutfordringer når det er aktuelt.
5. Kjør relevante bygge- og testkommandoer og oppdater berørt dokumentasjon.

Spesifikasjonen er kilden til sannhet for API-kontrakten. Kodegenerering,
kompilering og tester skal kunne kjøres fra en ren utsjekking i CI.

## Midlertidige filer og logger

Legg logger som opprettes under arbeidet i repoets `tmp/`-katalog, for eksempel
`mvn clean verify -l tmp/build.log`. Katalogen er ignorert av Git.
Rydd bort midlertidige logger når de ikke lenger trengs.

## Commit-beskjeder

- Start commit-beskjeder med et beskrivende prefiks i store bokstaver og
  hakeparenteser, for eksempel `[FEATURE]`, `[BUGFIX]`, `[DOCS]`, `[REFACTOR]`,
  `[TEST]` eller `[CI]`. Velg prefiks etter typen endring.
- Følg prefikset med en kort, konkret beskrivelse av hva som er utført.
  Ta med nok detaljer til at endringen er tydelig uten å måtte lese diffen.
  Unngå vage beskrivelser som «oppdateringer» eller «fikser».
- Bruk en kort utdyping i commit-teksten når det er nødvendig for å forklare
  endringen eller hvorfor den ble gjort.

Eksempel: `[DOCS] Dokumenter PostgreSQL-lagring og støtte for Podman og Docker`.

## Status og valg som gjenstår

`scripts/install-windows.ps1` og `scripts/install-unix.sh` installerer lokale
byggeverktøy og spør om Podman eller Docker. Begge har en planmodus uten
endringer. Unix støtter macOS, Ubuntu, Debian og Fedora på x86_64/ARM64.
Maven er låst til 3.9.16; JDK velges fra Temurin 25 LTS med tilgjengelig
plattformpatch. Containerverktøy bruker stabile pakker fra pakkebehandlerne.
Podman bruker Docker Compose på Windows og podman-compose på Unix.
Eksisterende Docker beholder sin Compose-plugin, eller får Compose 5.5.1
som reserve på Unix. Full installasjon på rene maskiner er ikke verifisert.
Se README.md for forutsetninger, miljøvariabler og førstegangsoppsett.

Prosjektet har Maven-parent og bruker lokalt installert Maven 3.9.16,
`spec/openapi.yaml` og modulen `api`. Modulen validerer kontrakten
og genererer Kotlin-DTO-er og Spring API-grensesnitt i `target/`.
Modulen `service` implementerer `HealthApi`, `PersonApi`, `RoomApi`,
`ActivityApi`, `AccommodationApi` og `ParticipationApi`. `GET /health` svarer
med HTTP 200 og `{"status":"UP"}`. Dette er en livssjekk for API-et og kontrollerer
ikke databasetilkoblingen. Det tidligere `/ping`-endepunktet er fjernet.
Person-API-et tilbyr `GET /persons`,
`GET /persons/{personId}` og `POST /persons`; kontrakten genererer egne DTO-er
for oppretting og respons. HTTP-testene starter Spring Boot på tilfeldige porter
og trenger ikke en ekstern database fordi databaseautokonfigurasjonen deaktiveres
og berørte lag mockes.
Kjør `mvn clean verify` med JDK 25. Se README.md for detaljer.

Rom-API-et er read-only og tilbyr `GET /rooms` og
`GET /rooms/{roomNumber}`. Romresponsen inneholder romnummer, størrelse (`size`),
antall senger (1, 2 eller 4), balkongflagg og siste renoveringsdato.

Datoformatene er bevisst ulike som en bestilt integrasjonsutfordring:
aktiviteter bruker dato-/klokkeslettstrenger som `13.oct 09:00` uten år/tidssone,
rom bruker `LocalDate` / PostgreSQL `DATE` satt til 1. januar i renoveringsåret,
og personens `registrationDate` bruker `OffsetDateTime` / PostgreSQL
`TIMESTAMP WITH TIME ZONE`. Nye personer får gjeldende tidspunkt i UTC.
Romfeltets eksisterende API-identifikator `lastRenovatedYear` er bevart,
men verdien er nå en dato som `2024-01-01`. Databasen håndhever 1. januar
og år fra 1900 til 2100. V1–V4 og V7–V8 er endret direkte med nye dato-/tidsverdier
i startdataene; eksisterende databaser må resettes eksplisitt av utvikleren.
API og deltakerrettet dokumentasjon beskriver formatene nøytralt uten
å avsløre læringsformålet. Oppgave 1–4 er implementert.
Verifisert 24. september 2026 med `mvn --batch-mode --no-transfer-progress clean verify`
mot isolert PostgreSQL 18.6: alle 62 tester bestod uten hoppede tester,
inkludert JSON-formatene i liste-, detalj- og nestede responser, bevaring av
registreringstidspunkt ved lagring med offset, databaseregler for romdatoer,
aktiviteters månedsformat, datosortering og avvisning av aktivitet over midnatt.

`gender` bruker nå `Int` / PostgreSQL `INTEGER`: 1 = mann, 2 = kvinne,
0 = annet. `PersonService` normaliserer alle andre int32-verdier til 0 før
lagring. Databasen tillater bare 0, 1 og 2. V1–V2 er oppdatert direkte,
og eksisterende databaser må resettes. OpenAPI oppgir bare påkrevd
`integer` / `int32`, uten enum, verdiområde, eksempel eller beskrivelse
av kodene eller normaliseringen. Dette er en bestilt integrasjonsutfordring;
ikke dokumenter betydningen eller normaliseringen i API-et eller
deltakerrettet dokumentasjon. Alle personresponser, også nestede, bruker heltall.
Verifisert 24. september 2026 med `mvn --batch-mode --no-transfer-progress clean verify`
mot isolert PostgreSQL 18.6: alle 75 tester bestod uten hoppede tester.
HTTP-testene dekker 0, 1, 2, ukjente verdier og int32-grensene gjennom det ekte
servicelaget. Manglende/null `gender`, tekst og heltall utenfor int32 gir 400.
Databasetestene dekker konverterte startdata, lagring av normalisert verdi
og avvisning av ukjente koder ved direkte repository-skriving.

Romstørrelsen eksponeres nå som `size` i `RoomResponse`, `RoomDetailsResponse`
og personens `assignedRoom`. Feltet er fortsatt et påkrevd positivt heltall,
men måleenheten dokumenteres ikke i API-et eller deltakerrettet dokumentasjon.
Dette er en bestilt integrasjonsutfordring. Kotlin-entiteten bruker `Room.size`,
og databasekolonnen heter `size`, med samme verdier og validering.
V3–V4 er endret direkte, så eksisterende databaser må resettes.
API-et returnerer ikke lenger `sizeSquareMeters`.
Generatoren kaller Kotlin-feltet `propertySize` og annoterer det med
`@JsonProperty("size")`; servicelagene mapper til dette genererte feltet.
Verifisert 24. september 2026 med `mvn --batch-mode --no-transfer-progress clean verify`
mot isolert PostgreSQL 18.6: alle 75 tester bestod uten hoppede tester.
HTTP-testene kontrollerer `size` og fravær av `sizeSquareMeters` i romliste,
romdetaljer og personens nestede rom. Bygget er også verifisert etter endringen
av databasekolonnen; SQL-oppslag bekrefter at `room` har `size INTEGER` og
ingen kolonne med det tidligere navnet.

`service` bruker Spring Data JPA, Flyway 12.4.0, PostgreSQL-modulen for Flyway
og PostgreSQL JDBC 42.7.13. `V1__create_person_table.sql` oppretter tabellen
`person` med identity-ID, navn, avdeling, e-post, telefonnummer, kjønn og
registreringsdato. Tekstfeltene er påkrevde og kan ikke være blanke; e-post er
unik uavhengig av store/små bokstaver. `V2__seed_person_table.sql` legger inn
20 deterministiske, fiktive personer. Flyway kjører seedingen bare ved første
initialisering, slik at vanlig omstart ikke overskriver, gjenoppretter eller
dupliserer data. Reset av databasevolumet gjenoppretter de opprinnelige dataene.

`V3__create_room_table.sql` oppretter `room` med romnummer som primærnøkkel,
størrelse, antall senger, balkongflagg og siste renoveringsdato.
`V4__seed_room_table.sql` legger inn 20 deterministiske rom med en blanding av
enerom, tomannsrom og firemannsrom. Flyways migreringshistorikk gjør at rommene
ikke dupliseres eller gjenopprettes ved vanlig omstart.

`Person` er både personmodell og JPA-entitet. `PersonRepository` arver
`JpaRepository`; `save` legger til personer, mens `findPersonById` og `findAll`
bruker JPQL deklarert med `@Query`. Kotlin-kompileringen bruker `spring`- og
`jpa`-pluginene for proxybare Spring-klasser og JPA-kompatible entiteter.
Hibernate bruker `ddl-auto=validate`, mens Flyway alene eier skjemaendringer.
Open EntityManager in View er deaktivert.
Repository-integrasjonstestene krever en eksplisitt, separat PostgreSQL-
testdatabase gjennom miljøvariablene `REISEAPP_TEST_DATABASE_URL`,
`REISEAPP_TEST_DATABASE_USER` og `REISEAPP_TEST_DATABASE_PASSWORD`; ellers
hoppes de over. Testene er verifisert mot en isolert PostgreSQL 18.6-database.

`PersonService` er et konkret servicelag uten eget interface. Det tilbyr
`add`, `findById` og `findAll`, eier transaksjonsgrensene og delegerer til
`PersonRepository`. Det mapper mellom genererte API-DTO-er og JPA-entiteten med
private funksjoner direkte i servicelaget; lesemetodene bruker read-only-
transaksjoner. `PersonController` implementerer det genererte `PersonApi`-
grensesnittet og håndterer bare HTTP-responsene. Servicelaget har isolerte
enhetstester med mocket repository, og controllerens HTTP-oppførsel og
inputvalidering er testet separat.

`RoomRepository` bruker JPQL for detaljoppslag og sortert liste. `RoomService`
eier read-only-transaksjonene og mapper til generert DTO, mens `RoomController`
implementerer `RoomApi`. Romlaget har enhets- og HTTP-tester; repositorytesten
kjøres sammen med de øvrige databaseintegrasjonstestene når testdatabasen er
konfigurert.

Romfordeling lagres i `person_room` via JPA-entiteten `PersonRoom`.
V5 oppretter tabellen med person-ID som primærnøkkel (høyst ett rom per person),
fremmednøkler og indeks på romnummer. V6 seeder 20 koblinger til ti rom innenfor
sengekapasiteten, ved oppslag på startpersonenes e-postadresser.
`PersonRoomRepository` tilbyr eksplisitte JPQL-spørringer i begge retninger.
`PersonService.findById` returnerer `PersonDetailsResponse` med valgfri
`assignedRoom`; `RoomService.findByNumber` returnerer `RoomDetailsResponse`
med `persons` sortert på ID (tom liste for ledige rom). Nestede objekter bruker
grunnresponsene uten tilbakekobling. Liste- og opprettingsresponsene er uendret;
bare detaljoppslag henter koblingene. Ingen nye endepunkter er innført.
Romtildeling kan nå endres med `PUT /persons/{personId}/room`
(`AssignRoomRequest` med `roomNumber`) og `DELETE /persons/{personId}/room`.
`AccommodationController` implementerer `AccommodationApi` og mapper resultatene
fra `AccommodationService` til 204, 404 eller 409; valideringsfeil gir 400.
PUT kan flytte mellom rom atomisk; fullt målrom gir 409 og bevarer gammel tildeling.
PUT til samme rom og DELETE for en person uten rom er idempotente (204).
Servicelaget bruker READ_COMMITTED og pessimistisk skrivelås på personen først,
deretter gammelt/nytt rom i stigende romnummer. Kapasitet telles etter romlåsen
og låsene beholdes til commit. Alle endringer i romtildelinger må følge dette
låseregimet for å bevare kapasitet og unngå deadlock mellom motsatte flyttinger.
Ingen skjemaendringer er nødvendige. Verifisert med `mvn clean verify` mot isolert
PostgreSQL 18.6: alle 42 tester bestod, inkludert samtidige tildelinger til siste
seng, samtidige tildelinger for samme person, fjerning, flytting og HTTP-validering.
Verifisert 17. september 2026 med `mvn clean verify` mot en isolert PostgreSQL
18.6-container: alle 19 tester bestod, inkludert V1–V6, Hibernate-validering,
JPQL-oppslag i begge retninger og kontroll av seedet sengekapasitet.

Aktivitets-API-et tilbyr bare `GET /activities` og
`GET /activities/{activityId}`. Grunnresponsen `ActivityResponse` har ID,
tittel, beskrivelse, maksantall deltakere, start/slutt som strenger som `13.oct 09:00`
og `notes` som tekst med HTML-tagger (tom streng når det ikke finnes råd).
Listen sorteres på starttidspunkt og ID.
V7 oppretter `activity` med `notes` (TEXT NOT NULL DEFAULT '') og regler for positiv
kapasitet og varighet fra 2 til 8 timer inklusive, med slutt etter start samme dag.
V8 seeder fem aktiviteter 13. oktober 2026, med passende praktiske råd.
Dato og klokkeslett lagres som PostgreSQL `TIMESTAMP WITHOUT TIME ZONE` og
Kotlin `LocalDateTime`. Databasen håndhever at start og slutt er på samme dato.
`toActivityTimeString` formaterer både direkte og nestede API-responser med
dag uten innledende null, engelsk månedsforkortelse med små bokstaver og `HH:mm`.
Manglende årstall og tidssone er en uttrykkelig bestilt integrasjonsutfordring:
klienten får dag og måned, men kan ikke utlede år eller tidssone fra API-et.
Bevar denne begrensningen og ikke eksponer år/tidssone uten bestilling.
Ikke avslør læringsformålet eller omtale datakvaliteten som bevisst dårlig
i API-kontrakten, API-responser eller deltakerrettet dokumentasjon.
Beskriv format og oppførsel nøytralt; begrunnelsen beholdes i prosjektkonteksten her.
`Activity` mapper `notes` som et vanlig String-felt.
V8 seeder HTML-notater for alle fem aktiviteter med avsnitt, linjeskift,
utheving og lister (`p`, `br`, `strong`, `em`, `ul`, `ol`, `li`). Taggene lagres
i TEXT-feltet og returneres uendret som del av JSON-strengen i aktivitetsliste,
aktivitetsdetaljer og personens nestede aktivitet. Ingen HTML-rensing eller
HTML-escaping legges inn i backend. Dette er en uttrykkelig bestilt
integrasjonsutfordring: frontend må velge hvordan innholdet skal behandles
og vises. Startdataene bruker bare formatering, uten skript eller hendelsesattributter.
V8 er endret direkte, så eksisterende databaser må resettes for nye startdata.
Verifisert 24. september 2026 med `mvn --batch-mode --no-transfer-progress clean verify`
mot isolert PostgreSQL 18.6: alle 75 tester bestod uten hoppede tester.
Testene kontrollerer HTML i seedede notater, uendrede tagger og HTML-entiteter
i liste-, detalj- og nestede HTTP-responser, samt tom streng uten notater.
`ActivityRepository` bruker JPQL for liste og detaljoppslag.
`ActivityService` mapper innenfor read-only-transaksjoner.
Verifisert med `mvn clean verify` mot isolert PostgreSQL 18.6:
alle 34 tester bestod, inkludert HTTP-klokkeslett, mapping, migreringer,
varighetsregler og rom-/aktivitetskapasitet for de utvidede startdataene.

V9 oppretter `person_activity` med person-ID som primærnøkkel, fremmednøkler
og indeks på aktivitets-ID. En person kan ha 0–1 aktivitet, en aktivitet
0–mange personer. V10 seeder 18 personer: fire på hver av aktivitet 1–4 og
to på aktivitet 5. Person 9 og 10 har ingen aktivitet. De ti nye personene
(ID 11–20) har alle både rom og aktivitet.
`PersonDetailsResponse.activity` er valgfri `ActivityResponse`.
Aktivitetsdetaljer bruker `ActivityDetailsResponse` med `participants`
som liste av `PersonResponse`, sortert på person-ID, tom ved ingen deltakere.
Listene returnerer grunnopplysninger uten koblinger. `PersonService` og
`ActivityService` henter koblingene fra `PersonActivityRepository` med JPQL.
Databasetester verifiserer kardinalitet, fremmednøkler og begge oppslagsretninger.

`ParticipationController` implementerer `ParticipationApi` med
`PUT /persons/{personId}/activity` (`EnrollInActivityRequest.activityId`) og
`DELETE /persons/{personId}/activity`. `ParticipationService` returnerer
resultater som controlleren mapper til 204, 404 eller 409; inputvalidering gir 400.
PUT bytter aktivitet atomisk og bevarer gammel påmelding ved fullt mål (409).
Gjentatt PUT til samme aktivitet og DELETE uten påmelding gir 204.
Service bruker READ_COMMITTED, låser personen først og deretter berørte aktiviteter
i stigende ID-rekkefølge. Kapasiteten telles etter låsing, og låsene holdes til
commit. Alle påmeldingsendringer må følge samme låseregime.
Ingen skjemaendringer er nødvendige. Verifisert med `mvn clean verify` mot
isolert PostgreSQL 18.6: alle 51 tester bestod, inkludert konkurranse om siste
plass, samme persons samtidige påmeldinger, motsatte aktivitetsbytter,
avmelding, uendret påmelding ved feil og HTTP-validering.

Start tjenesten etter bygg med `java -jar service/target/service-0.1.0-SNAPSHOT.jar`.
`service/Dockerfile` pakker Maven-byggets JAR i
`docker.io/library/eclipse-temurin:25.0.4_7-jre-noble` (Linux JRE 25.0.4+7,
Ubuntu 24.04 LTS). Denne eksplisitte Linux-image-taggen er valgt fra Temurins
offisielle image-liste; lokal Windows-JDK er fortsatt 25.0.4.1+1.
Bygg med `podman build -t localhost/workshop-reiseapp:dev ./service` eller
tilsvarende `docker build`, etter `mvn clean verify`.
Felles `compose.yaml` starter tjenesten og PostgreSQL 18.6 i separate containere
med `podman compose up --build -d` eller `docker compose up --build -d`.
Databaseimaget er låst til `docker.io/library/postgres:18.6-trixie`. Et navngitt
volum montert på `/var/lib/postgresql` bevarer data. Databasen eksponeres bare
på localhost og har en helsesjekk som tjenesten venter på. Se README.md for
konfigurasjon, direkte kjøring uten Compose, nedstenging og eksplisitt reset.
`scripts/run-windows.ps1` og `scripts/run-unix.sh` tilbyr en samlet lokal
oppstart: preflight-sjekk, `mvn clean verify`, Compose-bygging og start av begge
containerne i bakgrunnen. Begge spør om Podman eller Docker når begge finnes,
eller kan få runtime eksplisitt som argument. Begge støtter valgfrie host-porter,
eksplisitt stopp med bevaring av databasevolumet og stopper et eksisterende
Compose-oppsett før ny build/start.
Utvikleren har bygget imaget med Podman. Oppstart, HTTP 200 fra API-et fra
Windows via localhost:8080 og nedstenging er verifisert med Podman 6.0.2
i rootless-modus. Rootful-oppsettet på denne Windows/WSL2-maskinen videresendte
ikke porten til Windows; bytte til rootless løste problemet. Se README.md.
PostgreSQL-imaget er verifisert separat med oppstart, readiness, SQL og bevaring
av data gjennom ny container mot PostgreSQL 18.6 med Podman 6.0.2. Det samlede
Compose-oppsettet er verifisert med Docker Compose 5.5.1 som Podman-provider:
bygg og test, image-bygging, databasehelse, HTTP, SQL, stopp med bevart volum og
gjenoppstart med bevarte data. Flyway V1 er verifisert mot PostgreSQL 18.6 med
korrekte kolonner, regler og indeks samt en tilbakerullet testinnsetting. Flyway
V2 er verifisert med ti startpersoner uten duplikater ved omstart. Docker Engine
er ikke verifisert.

`.github/workflows/pull-request.yml` bygger og tester pull requests mot `main`
med `mvn --batch-mode --no-transfer-progress clean verify` på Ubuntu 24.04.
CI bruker Temurin 25.0.4+7, Maven 3.9.16 (nedlastet med SHA-512-kontroll)
og PostgreSQL 18.6-trixie som separat servicecontainer. Alle tre
`REISEAPP_TEST_DATABASE_*`-variablene settes slik at databasetestene aktiveres.
Workflowen har bare lesetilgang til repoet, cacher Maven-avhengigheter og
avbryter eldre kjøringer for samme PR. Kjøring på GitHub er ennå ikke verifisert.

Teknologiversjoner og generator er valgt i tabellen over.
Dokumenter de faktiske kommandoene for bygg, test og lokal kjøring når
oppsettet er på plass, og verifiser versjonskombinasjonen da.
