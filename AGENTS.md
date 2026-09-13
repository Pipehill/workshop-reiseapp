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
- Prosjektet bygges med Maven.
- API-ene utvikles spec first med OpenAPI.
- Endepunkter og datamodeller spesifiseres i OpenAPI før de implementeres.
- API-kode genereres fra spesifikasjonen som del av Maven-bygget.
- Koden skal ligge på GitHub.
- GitHub Actions skal bygge og teste koden for hver pull request.

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

## Status og valg som gjenstår

Prosjektet er i oppstartsfasen. Bygg, API-spesifikasjon, applikasjonskode og
CI er foreløpig ikke opprettet.

Backendrammeverk, JDK- og Kotlin-versjon, PostgreSQL-versjon, OpenAPI-versjon,
generator og konkrete endepunkter er ennå ikke valgt. Ikke behandle
eksempler eller forslag som vedtatte valg. Dokumenter valgene og de faktiske
kommandoene for bygg, test og lokal kjøring når oppsettet er på plass.
