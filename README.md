# workshop-reiseapp

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
