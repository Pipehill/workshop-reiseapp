#Requires -Version 5.1
[CmdletBinding()]
param(
    [ValidateSet('podman', 'docker')]
    [string]$ContainerRuntime,
    [switch]$Plan
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$mavenVersion = '3.9.16'

if (-not $ContainerRuntime) {
    do {
        $choice = (Read-Host 'Velg containerverktøy: 1 = Podman (anbefalt), 2 = Docker').Trim().ToLowerInvariant()
        switch ($choice) {
            { $_ -in '1', 'podman' } { $ContainerRuntime = 'podman' }
            { $_ -in '2', 'docker' } { $ContainerRuntime = 'docker' }
            default { Write-Host 'Skriv 1 eller 2.' }
        }
    } until ($ContainerRuntime)
}

Write-Host "Plan: Temurin JDK 25, Maven $mavenVersion og $ContainerRuntime med Compose."
Write-Host 'Java/containerverktøy installeres med WinGet. Maven installeres for din bruker.'
Write-Host 'JAVA_HOME og brukerens PATH oppdateres. WSL/desktop-oppstart beskrives til slutt.'
if ($Plan) { return }
if ($env:OS -ne 'Windows_NT') { throw 'Bruk install-unix.sh på Unix.' }
if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    throw 'Installer App Installer (WinGet) fra Microsoft Store og kjør skriptet på nytt.'
}

function Install-WingetPackage([string]$PackageId) {
    & winget list --id $PackageId --exact --source winget --accept-source-agreements | Out-Host
    if ($LASTEXITCODE -eq 0) {
        Write-Host "$PackageId er allerede installert; beholdes."
        return
    }
    & winget install --id $PackageId --exact --source winget --accept-source-agreements --accept-package-agreements | Out-Host
    if ($LASTEXITCODE -eq 3010) {
        Write-Warning 'Installasjonen krever omstart av Windows. Start på nytt og kjør skriptet igjen.'
        return
    }
    if ($LASTEXITCODE -ne 0) { throw "Installasjon av $PackageId feilet (exit $LASTEXITCODE)." }
}

Install-WingetPackage 'EclipseAdoptium.Temurin.25.JDK'

# Locate the JDK even when this PowerShell session still has the old PATH.
$jdkCandidates = @($env:JAVA_HOME)
foreach ($jdkRoot in @("$env:ProgramFiles/Eclipse Adoptium", "$env:LOCALAPPDATA/Programs/Eclipse Adoptium")) {
    if (Test-Path -LiteralPath $jdkRoot) {
        $jdkCandidates += @(Get-ChildItem -LiteralPath $jdkRoot -Directory -Filter 'jdk-25*' |
            Sort-Object Name -Descending | ForEach-Object { $_.FullName })
    }
}
$selectedJdk = $null
foreach ($jdkCandidate in $jdkCandidates) {
    if (-not $jdkCandidate) { continue }
    $jdkRelease = Join-Path $jdkCandidate 'release'
    if ((Test-Path -LiteralPath $jdkRelease) -and
        (Test-Path -LiteralPath (Join-Path $jdkCandidate 'bin/javac.exe')) -and
        ((Get-Content -LiteralPath $jdkRelease -Raw) -match 'JAVA_VERSION="25(?:\.|"|\+)')) {
        $selectedJdk = $jdkCandidate
        break
    }
}
if (-not $selectedJdk) { throw 'JDK 25 ble ikke funnet. Start en ny terminal, sett JAVA_HOME til JDK 25 og kjør igjen.' }

$toolsRoot = Join-Path $env:LOCALAPPDATA 'workshop-reiseapp/tools'
$mavenHome = Join-Path $toolsRoot "apache-maven-$mavenVersion"
if (-not (Test-Path -LiteralPath (Join-Path $mavenHome 'bin/mvn.cmd'))) {
    $downloadDir = Join-Path $toolsRoot 'downloads'
    New-Item -ItemType Directory -Path $downloadDir -Force | Out-Null
    $archive = Join-Path $downloadDir "apache-maven-$mavenVersion-bin.zip"
    $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $archive
    $expectedHash = ((Invoke-WebRequest -UseBasicParsing -Uri "$url.sha512").Content.Trim() -split '\s+')[0]
    if ($expectedHash -notmatch '^[a-fA-F0-9]{128}$' -or
        (Get-FileHash -LiteralPath $archive -Algorithm SHA512).Hash -ne $expectedHash) {
        throw 'Maven-arkivets SHA-512 stemmer ikke. Installasjonen er stoppet.'
    }
    Expand-Archive -LiteralPath $archive -DestinationPath $toolsRoot -Force
}

[Environment]::SetEnvironmentVariable('JAVA_HOME', $selectedJdk, 'User')
$env:JAVA_HOME = $selectedJdk
$toolPaths = @((Join-Path $mavenHome 'bin'), (Join-Path $selectedJdk 'bin'))
$oldUserPath = [Environment]::GetEnvironmentVariable('Path', 'User')
$newUserPath = (@($toolPaths) + @($oldUserPath -split ';' | Where-Object { $_ -and $_ -notin $toolPaths })) -join ';'
[Environment]::SetEnvironmentVariable('Path', $newUserPath, 'User')
$env:Path = ($toolPaths -join ';') + ';' + $env:Path
& (Join-Path $mavenHome 'bin/mvn.cmd') --version
if ($LASTEXITCODE -ne 0) { throw 'Maven-kontrollen feilet.' }

if ($ContainerRuntime -eq 'podman') {
    Install-WingetPackage 'RedHat.Podman'
    Install-WingetPackage 'Docker.DockerCompose'
} else {
    Install-WingetPackage 'Docker.DockerDesktop'
}

Write-Host ''
Write-Host 'Installasjonstrinn fullført. Åpne en NY terminal og kontroller: java -version; mvn --version'
Write-Host 'Hvis WSL2 mangler: kjør wsl --install --no-distribution som administrator og start Windows på nytt.'
if ($ContainerRuntime -eq 'podman') {
    Write-Host 'Kontroller podman machine list. Hvis ingen maskin finnes: podman machine init --rootful=false'
    Write-Host 'Start maskinen med podman machine start <maskinnavn>. Eksisterende maskiner endres ikke.'
    Write-Host 'Kontroller: podman info; podman compose version'
} else {
    Write-Host 'Start Docker Desktop, fullfør førstegangsoppsettet og velg Linux-containere.'
    Write-Host 'Kontroller: docker info; docker compose version'
}
Write-Host 'Fra prosjektroten: mvn clean verify'
