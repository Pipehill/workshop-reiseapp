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

Kontrakten inneholder foreløpig `GET /ping` og modellen `PingResponse` som et
minimalt eksempel. Tjenesten svarer med `{"message":"pong"}` og HTTP 200.

## Bygg

### Installer avhengigheter

Skriptene spør om du vil bruke **Podman** (anbefalt) eller **Docker**.
De installerer JDK 25, Maven 3.9.16 og valgt containerverktøy med Compose-støtte.
Kotlin, Spring Boot og kodegeneratoren lastes ned av Maven under bygg;
PostgreSQL skal senere kjøres som container og installeres ikke på vertsmaskinen.

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

## Kjør tjenesten

Etter `mvn clean verify`, start tjenesten med JDK 25:

```sh
java -jar service/target/service-0.1.0-SNAPSHOT.jar
```

Tjenesten lytter på port 8080. Åpne `http://localhost:8080/ping` i en nettleser
eller kjør `curl http://localhost:8080/ping`. Forventet svar:

```json
{"message":"pong"}
```

Ingen autentisering eller database kreves. Stopp tjenesten med Ctrl+C.

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
Direktekommandoene over krever ingen Compose-provider.

| Handling | Podman | Docker |
| --- | --- | --- |
| Kontroller oppsett | `podman compose config` | `docker compose config` |
| Bygg image og start | `podman compose up --build -d` | `docker compose up --build -d` |
| Se logger | `podman compose logs -f service` | `docker compose logs -f service` |
| Stopp og fjern containere/nettverk | `podman compose down` | `docker compose down` |

Port 8080 må være ledig; stopp eventuell tidligere lokal kjøring først.
Ved kodeendringer kjøres `mvn clean verify` før containerbildet bygges på nytt.
Compose bygger bare containerbildet, ikke Kotlin-koden.

Oppsettet starter foreløpig bare tjenesten, som ikke bruker database.
Databasevolum og reset legges til sammen med PostgreSQL-integrasjonen.
Containerbildet er bygget med Podman av utvikleren. Oppstart, HTTP-kall fra
Windows og nedstenging er verifisert med Podman 6.0.2 i rootless-modus på WSL2.
Docker og Compose-kjøring er ennå ikke verifisert.

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
