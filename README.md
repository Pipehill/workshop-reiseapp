# Workshop: Reiseapp

Backend til en fiktiv reiseapp for opplæring og workshops i API-integrasjon.
Prosjektet skal bruke Kotlin, Maven og OpenAPI med spec first og kodegenerering.

Se [AGENTS.md](AGENTS.md) for prosjektkontekst og føringer for utviklere og
KI-kodeassistenter. Be verktøy som ikke leser filen automatisk, om å lese den
før de gjør endringer i prosjektet.

## Struktur

- `spec/openapi.yaml`: OpenAPI-kontrakten, kilden til sannhet for API-et.
- `pom.xml`: Maven-parent med felles versjoner og moduler.
- `api/`: genererer og kompilerer Kotlin-DTO-er og Spring API-grensesnitt.
- `service/`: kjørbar Spring Boot-tjeneste som implementerer API-grensesnittene.
- `service/src/main/resources/db/migration/`: versjonerte Flyway-migreringer.

Kontrakten inneholder foreløpig `GET /ping` og modellen `PingResponse` som et
minimalt eksempel. Tjenesten svarer med `{"message":"pong"}` og HTTP 200.

## Bygg

### Installer avhengigheter

Skriptene spør om du vil bruke **Podman** (anbefalt) eller **Docker**.
De installerer JDK 25, Maven 3.9.16 og valgt containerverktøy med Compose-støtte.
Kotlin, Spring Boot og kodegeneratoren lastes ned av Maven under bygg;
PostgreSQL 18.6 kjøres som container og installeres ikke på vertsmaskinen.

Windows, i PowerShell fra prosjektroten:

```powershell
.\scripts\install-windows.ps1
```

Krever PowerShell 5.1+ og WinGet (App Installer fra Microsoft Store).
Installasjonsprogrammene kan be om administratorrettigheter.
Hvis din lokale PowerShell-policy blokkerer skriptet, kan du kjøre det med
`powershell -ExecutionPolicy Bypass -File .\scripts\install-windows.ps1`;
dette gjelder bare denne prosessen.

Unix, i terminalen fra prosjektroten:

```sh
bash scripts/install-unix.sh
```

Støtter macOS med Homebrew, Ubuntu, Debian og Fedora, på x86_64 og ARM64.
Andre Unix-systemer stopper med en forklaring. Kjør som vanlig bruker;
Linux-skriptet bruker `sudo` når det installerer systempakker.

For å se planen uten installasjon:

```powershell
.\scripts\install-windows.ps1 -Plan
```

```sh
bash scripts/install-unix.sh --plan
```

Valget kan også gis direkte med `-ContainerRuntime podman` på Windows eller
`--runtime podman` på Unix (bruk `docker` for Docker).

Windows bruker WinGet for Temurin 25 og containerverktøy. Maven installeres
i `%LOCALAPPDATA%\workshop-reiseapp\tools`; brukerens `JAVA_HOME` og `PATH`
oppdateres. Åpne en ny terminal etter installasjon.

Unix installerer Temurin 25 og Maven under `~/.local/share/workshop-reiseapp/tools`.
Aktiver dem i terminalen med:

```sh
source ~/.local/share/workshop-reiseapp/tools/env.sh
```

Gjenta dette i nye terminaler, eller legg linjen i din Bash/Zsh-profil.
Homebrew/distribusjonens pakkebehandler brukes for Podman. Docker installeres
fra Docker Desktop på macOS og Dockers stabile pakkearkiv på Linux.
Eksisterende containerinstallasjoner beholdes. Manglende Compose legges til;
Unix bruker offisiell Compose 5.5.1 som reserve for eksisterende Docker.

Nyinstallasjoner bruker tilgjengelig stabil containerpakke fra pakkekilden
og siste Temurin 25-patch for plattformen. På Linux kan distribusjonens
Podman-versjon være eldre enn referanseversjonen i AGENTS.md. Maven er låst
til 3.9.16. Java/Maven-arkiver og eventuell separat Compose-binær kontrolleres
med sjekksummer før installasjon. Nedlastingene beholdes i `tools/downloads`.

Følg skriptets sluttmelding for WSL2, Podman-maskin eller førstegangsoppsett
av Docker Desktop. Skriptene starter ikke containere eller endrer eksisterende
Podman-maskiner. WSL2 kan kreve separat aktivering og omstart av Windows.

Syntaks og forhåndsvisning er kontrollert på Windows, Linux/Fedora og med
simulert macOS-plattform. Full installasjon er ikke testet på rene maskiner.

Installasjonskilder: [Adoptium](https://adoptium.net/installation/),
[Maven](https://maven.apache.org/download.cgi),
[Docker for Ubuntu](https://docs.docker.com/engine/install/ubuntu/) og
[Docker for Fedora](https://docs.docker.com/engine/install/fedora/).

### Bygg prosjektet

Installer JDK 25 og Maven 3.9.16. Sett `JAVA_HOME` til JDK-installasjonen og
legg Maven i `PATH`, slik at `mvn` er tilgjengelig. Første bygg krever
internettilgang for nedlasting av avhengigheter.

Kjør fra prosjektroten på Windows, Linux eller macOS:

```sh
mvn clean verify
```

Bygget validerer spesifikasjonen, genererer Kotlin-kode og kompilerer den
til `api/target/api-0.1.0-SNAPSHOT.jar`.
JAR-filen inneholder også spesifikasjonen under `META-INF/openapi/`.
Modulen `service` bruker `no.pipehill.reiseapp:api:0.1.0-SNAPSHOT`
som Maven-avhengighet og implementerer de genererte grensesnittene.

For kun validering og generering, kjør `generate-sources` i stedet for `verify`.
Generert kode ligger under `api/target/generated-sources/openapi/src/main/kotlin`:

- API-grensesnitt: `no.pipehill.reiseapp.api`
- DTO-er: `no.pipehill.reiseapp.api.dto`

Endre spesifikasjonen og bygg på nytt; generert kode skal ikke redigeres
eller sjekkes inn. Bruk `clean verify` etter sletting eller omdøping av
endepunkter/modeller slik at gamle genererte filer fjernes.

`verify` kontrollerer validering, generering, kompilering og pakking, og
kjører en HTTP-integrasjonstest som starter tjenesten på en tilfeldig port
og sjekker statuskode, innholdstype og JSON-respons for `/ping`.

Repository-integrasjonstestene aktiveres når `REISEAPP_TEST_DATABASE_URL`,
`REISEAPP_TEST_DATABASE_USER` og `REISEAPP_TEST_DATABASE_PASSWORD` er satt.
Bruk en egen, tom PostgreSQL-database til disse testene. Flyway initialiserer
databasen, og hver repositorytest kjøres i en transaksjon som rulles tilbake.
Uten testvariablene hoppes disse testene over, slik at standardbygget ikke
krever en kjørende database.

### Bygg og start med run-skript

Når Java, Maven og Podman eller Docker er installert, kan hele den lokale
oppstarten gjøres med ett skript. Skriptet kjører først en preflight-sjekk av
verktøyene, container-runtime og Compose, deretter `mvn clean verify`,
image-bygging og oppstart av tjenesten og PostgreSQL. Hvis begge runtime-ene er
installert, blir du bedt om å velge hvilken som skal brukes.

Før du starter må Java 25, Maven 3.9.16 og enten Podman eller Docker være
installert. Podman machine eller Docker Desktop må også være startet på
Windows og macOS.

Skriptet kontrollerer verktøyene og runtime-en, sjekker at port 8080 og 5432 er
ledige, kjører `mvn clean verify` og starter Compose-oppsettet i bakgrunnen.
PostgreSQL må være frisk før API-containeren startes.

Windows PowerShell:

```powershell
.\scripts\run-windows.ps1
```

Unix:

```sh
bash scripts/run-unix.sh
```

Runtime kan også velges direkte:

```powershell
.\scripts\run-windows.ps1 -Runtime podman
```

```sh
bash scripts/run-unix.sh --runtime podman
```

Bytt `podman` med `docker` for å velge Docker direkte.

Port 8080 brukes som standard. Velg en annen port slik:

```powershell
.\scripts\run-windows.ps1 -Port 9090
```

```sh
bash scripts/run-unix.sh --port 9090
```

API-containeren lytter fortsatt på port 8080 internt; parameteren endrer bare
porten på vertsmaskinen. PostgreSQL eksponeres på localhost port 5432. Velg en
annen databaseport med `-DatabasePort 55432` på Windows eller
`--database-port 55432` på Unix.

Skriptet starter begge containerne i bakgrunnen. Test API-et med
`curl http://localhost:8080/ping`, og følg alle logger med
`podman compose logs -f` eller `docker compose logs -f`.

Stopp tjenesten og databasen gjennom skriptet:

```powershell
.\scripts\run-windows.ps1 -Stop
```

```sh
bash scripts/run-unix.sh --stop
```

Stopp fjerner containerne og Compose-nettverket, men beholder databasevolumet.
Når et run-skript startes på nytt, stopper det først et eksisterende
Compose-oppsett med valgt runtime. Du trenger derfor ikke å stoppe tjenestene
manuelt før en ny build.

Kjør skriptet på nytt etter kodeendringer. Det bygger prosjektet og imaget på
nytt før containeren startes igjen.

## Kjør tjenesten

Etter `mvn clean verify` kan tjenesten kjøres direkte med JDK 25 mot en startet
PostgreSQL-database. Start for eksempel bare databasen med
`podman compose up -d database`, og sett tilkoblingsvariablene før tjenesten
startes.

Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/reiseapp'
$env:SPRING_DATASOURCE_USERNAME = 'reiseapp'
$env:SPRING_DATASOURCE_PASSWORD = 'reiseapp-local'
java -jar service/target/service-0.1.0-SNAPSHOT.jar
```

Unix:

```sh
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/reiseapp \
SPRING_DATASOURCE_USERNAME=reiseapp \
SPRING_DATASOURCE_PASSWORD=reiseapp-local \
java -jar service/target/service-0.1.0-SNAPSHOT.jar
```

Tjenesten lytter på port 8080. Åpne `http://localhost:8080/ping` i en nettleser
eller kjør `curl http://localhost:8080/ping`. Forventet svar:

```json
{"message":"pong"}
```

Ved oppstart kobler tjenesten til databasen og kjører ventende Flyway-migreringer.
Den direkte kommandoen starter ikke PostgreSQL-containeren. Stopp tjenesten med
Ctrl+C.

## Kjør med Podman eller Docker

Installer Podman (foretrukket) eller Docker med støtte for Linux-containere.
På Windows/macOS må Podman-maskinen være startet (`podman machine start`;
kjør `podman machine init` først hvis du ikke har opprettet en maskin).
Ved bruk av Docker Desktop må denne være startet og bruke Linux-containere.

Bygg først JAR-filen med lokalt installert JDK 25 og Maven:

```sh
mvn clean verify
```

`service/Dockerfile` pakker JAR-filen i et Linux-image og kjører Java som
en bruker uten root-tilgang. Bygget bruker Temurin JRE 25.0.4+7 på Ubuntu
24.04 LTS (`25.0.4_7-jre-noble`), en eksplisitt tag fra
[Temurins offisielle image-liste](https://github.com/docker-library/official-images/blob/master/library/eclipse-temurin).
Containerens Linux-runtime er dermed 25.0.4+7; den lokale Windows-JDK-en er
25.0.4.1+1. Ingen Jib-plugin er nødvendig.

Alle kommandoene nedenfor kjøres fra prosjektroten. Velg enten Podman
eller Docker for bygg og kjøring; de har separate lokale image-lagre.

### Direkte med Podman

```sh
podman build -t localhost/workshop-reiseapp:dev ./service
podman run --rm --name workshop-reiseapp -p 127.0.0.1:8080:8080 localhost/workshop-reiseapp:dev
```

### Direkte med Docker

```sh
docker build -t localhost/workshop-reiseapp:dev ./service
docker run --rm --name workshop-reiseapp -p 127.0.0.1:8080:8080 localhost/workshop-reiseapp:dev
```

Åpne `http://localhost:8080/ping` og kontroller at svaret er
`{"message":"pong"}`. Stopp med Ctrl+C eller, fra en annen terminal,
`podman stop -t 30 workshop-reiseapp` / `docker stop -t 30 workshop-reiseapp`.
`--rm` fjerner containeren når den stopper. Image-et beholdes.

### Med Compose

Den samme `compose.yaml` brukes med begge verktøy. Docker trenger Compose-pluginen.
`podman compose` trenger en ekstern Compose-provider, for eksempel
`podman-compose` eller Docker Compose. Se [Podmans dokumentasjon](https://docs.podman.io/en/latest/markdown/podman-compose.1.html).
Installasjonsskriptene installerer en provider sammen med valgt runtime.
Direktekommandoene over starter bare API-et og krever ingen Compose-provider.

| Handling | Podman | Docker |
| --- | --- | --- |
| Kontroller oppsett | `podman compose config` | `docker compose config` |
| Bygg image og start | `podman compose up --build -d` | `docker compose up --build -d` |
| Start bare databasen | `podman compose up -d database` | `docker compose up -d database` |
| Se alle logger | `podman compose logs -f` | `docker compose logs -f` |
| Åpne psql | `podman compose exec database psql -U reiseapp -d reiseapp` | `docker compose exec database psql -U reiseapp -d reiseapp` |
| Stopp og fjern containere/nettverk | `podman compose down` | `docker compose down` |
| Nullstill databasen | `podman compose down --volumes` | `docker compose down --volumes` |

Port 8080 og 5432 må være ledige; stopp eventuell tidligere lokal kjøring først.
Ved kodeendringer kjøres `mvn clean verify` før containerbildet bygges på nytt.
Compose bygger bare containerbildet, ikke Kotlin-koden.

Oppsettet starter API-et og PostgreSQL 18.6 i separate containere. Databasen
heter `reiseapp`, og standardbrukeren heter `reiseapp` med passordet
`reiseapp-local`. Disse standardverdiene er kun ment for lokal utvikling.
De kan overstyres med miljøvariablene `REISEAPP_DATABASE_NAME`,
`REISEAPP_DATABASE_USER` og `REISEAPP_DATABASE_PASSWORD`. Vertportene kan
overstyres med `REISEAPP_API_PORT` og `REISEAPP_DATABASE_PORT` ved direkte bruk
av Compose. Initialiseringsverdiene brukes bare når volumet er tomt; endring av
navn, bruker eller passord for en eksisterende database krever reset.

Det navngitte volumet `postgres-data` monteres på `/var/lib/postgresql`, som er
volumplasseringen for det offisielle PostgreSQL-imaget fra versjon 18. Vanlig
`compose down` og run-skriptenes stoppkommando bevarer volumet og dataene.
`compose down --volumes` er en eksplisitt, destruktiv reset som sletter alle
lokale databasedata. Neste oppstart oppretter databasen, kjører migreringene på
nytt og gjenoppretter de ti opprinnelige personene.

### Databaseskjema

Spring Boot kjører Flyway 12.4.0 ved oppstart. Migreringen
`V1__create_person_table.sql` oppretter tabellen `person` med følgende kolonner:

| Kolonne | PostgreSQL-type | Regler |
| --- | --- | --- |
| `id` | `BIGINT` | Primærnøkkel, genereres alltid som identity |
| `name` | `VARCHAR(200)` | Påkrevd og kan ikke være blank |
| `department` | `VARCHAR(100)` | Påkrevd og kan ikke være blank |
| `email` | `VARCHAR(254)` | Påkrevd, ikke blank og unik uavhengig av store/små bokstaver |
| `phone_number` | `VARCHAR(32)` | Påkrevd og kan ikke være blank |
| `gender` | `VARCHAR(50)` | Påkrevd og kan ikke være blank |
| `registration_date` | `DATE` | Påkrevd, standard er databasens gjeldende dato |

`V2__seed_person_table.sql` legger inn ti fiktive personer med faste data.
Navnene består av adjektiv som fornavn og substantiv som etternavn; enkelte har
to fornavn eller etternavn. E-postadressene bruker det reserverte `.test`-domenet,
og telefonnumrene er åpenbart fiktive.

Flyway registrerer V2 etter første vellykkede kjøring. Derfor overskrives ikke
endrede personer, og slettede personer gjenopprettes ikke ved vanlig omstart.
En eksplisitt reset av databasevolumet kjører både V1 og V2 på nytt og gir den
opprinnelige starttilstanden. Flyway- og PostgreSQL JDBC-versjonene styres av
Spring Boot 4.1.1 dependency management.

### Repositorylag

`Person` er både applikasjonens personmodell og en JPA-entitet mappet til
`person`-tabellen. `PersonRepository` arver `JpaRepository`; `save` legger til
personer, mens `findPersonById` og `findAll` bruker JPQL-spørringer deklarert med
`@Query`. Ved opprettelse genererer PostgreSQL ID-en, mens applikasjonen setter
registreringsdatoen. Hibernate validerer Flyway-skjemaet ved oppstart og kan
ikke endre det. Open EntityManager in View er deaktivert.

PostgreSQL-imaget er låst til `docker.io/library/postgres:18.6-trixie`.
[PostgreSQL 18.6](https://www.postgresql.org/docs/18/release-18-6.html) er valgt
i tråd med prosjektets versjonsføringer, og image-taggen finnes i
[den offisielle image-listen](https://hub.docker.com/_/postgres/tags?name=18.6-trixie).

Containerbildet er bygget med Podman av utvikleren. Oppstart, HTTP-kall fra
Windows og nedstenging er verifisert med Podman 6.0.2 i rootless-modus på WSL2.
PostgreSQL-imaget er verifisert separat med oppstart, readiness, SQL og bevaring
av data gjennom ny container mot PostgreSQL 18.6 med Podman 6.0.2.
Compose-oppsettet er verifisert på Windows med Podman 6.0.2 i rootless-modus og
Docker Compose 5.5.1 som provider: bygg og test, image-bygging, venting på frisk
database, HTTP 200 fra API-et, SQL, stopp med bevart volum og gjenoppstart med
bevarte data. Docker Engine er ennå ikke verifisert.

### Windows: connection refused på localhost

På den testede maskinen svarte API-et inne i Podman-maskinen, men porten
var utilgjengelig fra Windows i rootful-modus. Bytte til rootless løste dette.
Hvis du opplever samme problem, sjekk først `podman ps`, `podman port <navn>`
og `podman logs <navn>` for å bekrefte oppstart og portkobling.

Rootless kan velges ved opprettelse av Podman-maskinen. En eksisterende maskin
kan byttes med kommandoene under (erstatt `<maskinnavn>` med navnet fra
`podman machine list`). Stopp containerne først:

```sh
podman machine stop <maskinnavn>
podman machine set --rootful=false <maskinnavn>
podman machine start <maskinnavn>
```

Rootful og rootless har separate image- og volumlagre. Data slettes ikke ved
byttet, men bygg app-imaget på nytt i rootless-modus før oppstart.
Dette endrer Podman-maskinen lokalt; Dockerfile og Compose-filen er uendret.
