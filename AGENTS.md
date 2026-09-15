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
produktversjon. Konkrete actions og eventuell Compose-provider velges og
låses når CI- og containeroppsettet lages.

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

1. Beskriv eller oppdater kontrakten i OpenAPI-spesifikasjonen først.
2. Generer API-koden gjennom Maven. Ikke rediger genererte filer manuelt.
3. Implementer oppførselen i håndskrevet Kotlin-kode adskilt fra generert kode.
4. Legg til relevante tester for kontrakten og oppførselen, inkludert tilsiktede
   integrasjonsutfordringer når det er aktuelt.
5. Kjør relevante bygge- og testkommandoer og oppdater berørt dokumentasjon.

Spesifikasjonen er kilden til sannhet for API-kontrakten. Kodegenerering,
kompilering og tester skal kunne kjøres fra en ren utsjekking i CI.

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
Modulen `service` implementerer `PingApi` og svarer med HTTP 200 og
`{"message":"pong"}` på `GET /ping`. En HTTP-integrasjonstest starter
Spring Boot på en tilfeldig port og kontrollerer responsen. Testen deaktiverer
JDBC- og Flyway-autokonfigurasjon og bruker en testlokal `PersonRepository`-mock,
og trenger derfor ikke en ekstern database.
Kjør `mvn clean verify` med JDK 25. Se README.md for detaljer.

`service` bruker Spring Data JPA, Flyway 12.4.0, PostgreSQL-modulen for Flyway
og PostgreSQL JDBC 42.7.13. `V1__create_person_table.sql` oppretter tabellen
`person` med identity-ID, navn, avdeling, e-post, telefonnummer, kjønn og
registreringsdato. Tekstfeltene er påkrevde og kan ikke være blanke; e-post er
unik uavhengig av store/små bokstaver. `V2__seed_person_table.sql` legger inn
ti deterministiske, fiktive personer. Flyway kjører seedingen bare ved første
initialisering, slik at vanlig omstart ikke overskriver, gjenoppretter eller
dupliserer data. Reset av databasevolumet gjenoppretter de opprinnelige dataene.

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
Utvikleren har bygget imaget med Podman. Oppstart, HTTP 200 med pong fra
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
er ikke verifisert. CI er ikke opprettet.

Teknologiversjoner og generator er valgt i tabellen over. Domeneendepunkter og
CI-actions er ennå ikke valgt.
Dokumenter de faktiske kommandoene for bygg, test og lokal kjøring når
oppsettet er på plass, og verifiser versjonskombinasjonen da.
