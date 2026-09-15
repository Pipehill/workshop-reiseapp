[CmdletBinding()]
param(
    [Alias('Runtime')]
    [string]$ContainerRuntime = 'auto',
    [ValidateRange(1, 65535)]
    [int]$Port = 8080,
    [ValidateRange(1, 65535)]
    [int]$DatabasePort = 5432,
    [switch]$Stop
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot

if ($ContainerRuntime -notin @('auto', 'podman', 'docker')) {
    throw "Invalid runtime '$ContainerRuntime'. Use auto, podman, or docker."
}
if ($Port -eq $DatabasePort) {
    throw 'The API port and database port must be different.'
}

function Require-Command([string]$Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name is not installed or not available in PATH. Run the installation script first."
    }
}

if ($ContainerRuntime -eq 'auto') {
    $available = @(@('podman', 'docker') | Where-Object {
        Get-Command $_ -ErrorAction SilentlyContinue
    })

    if ($available.Count -eq 0) {
        throw 'Neither podman nor docker was found. Run the installation script first.'
    }
    if ($available.Count -eq 1) {
        $ContainerRuntime = $available[0]
    } else {
        Write-Host 'Choose a container runtime:'
        for ($i = 0; $i -lt $available.Count; $i++) {
            Write-Host "$($i + 1): $($available[$i])"
        }
        do {
            $selection = Read-Host 'Enter a number'
        } while ($selection -notmatch '^[12]$' -or [int]$selection -gt $available.Count)
        $ContainerRuntime = $available[[int]$selection - 1]
    }
}

Require-Command $ContainerRuntime
& $ContainerRuntime info | Out-Null
& $ContainerRuntime compose version
if ($LASTEXITCODE -ne 0) {
    throw "$ContainerRuntime Compose is not available. Run the installation script first."
}

function Invoke-Compose([string[]]$ComposeArguments) {
    Push-Location $repoRoot
    try {
        & $ContainerRuntime compose @ComposeArguments
        if ($LASTEXITCODE -ne 0) { throw "$ContainerRuntime Compose failed." }
    } finally {
        Pop-Location
    }
}

function Stop-ExistingContainers {
    Write-Host 'Stopping existing application and database containers if present ...'
    Invoke-Compose -ComposeArguments @('down', '--remove-orphans')
}

if ($Stop) {
    Stop-ExistingContainers
    Write-Host 'The application and database containers are stopped. Database data is preserved.'
    exit 0
}

Require-Command 'java'
Require-Command 'mvn'

Write-Host "Checking Java, Maven, and $ContainerRuntime ..."
& java -version
& mvn -version

Stop-ExistingContainers
if (Test-NetConnection -ComputerName 127.0.0.1 -Port $Port -InformationLevel Quiet) {
    throw "Port $Port is already in use. Stop the service using it before starting again."
}
if (Test-NetConnection -ComputerName 127.0.0.1 -Port $DatabasePort -InformationLevel Quiet) {
    throw "Port $DatabasePort is already in use. Stop the database using it or select another database port."
}

Push-Location $repoRoot
try {
    Write-Host 'Building and testing the project ...'
    & mvn clean verify
    if ($LASTEXITCODE -ne 0) { throw 'The Maven build failed.' }
} finally {
    Pop-Location
}

$env:REISEAPP_API_PORT = $Port.ToString()
$env:REISEAPP_DATABASE_PORT = $DatabasePort.ToString()
$databaseName = if ([string]::IsNullOrWhiteSpace($env:REISEAPP_DATABASE_NAME)) { 'reiseapp' } else { $env:REISEAPP_DATABASE_NAME }
$databaseUser = if ([string]::IsNullOrWhiteSpace($env:REISEAPP_DATABASE_USER)) { 'reiseapp' } else { $env:REISEAPP_DATABASE_USER }
Write-Host "Building and starting the application and PostgreSQL with $ContainerRuntime Compose ..."
Invoke-Compose -ComposeArguments @('up', '--build', '-d')

Write-Host "The service is running at http://localhost:$Port/health"
Write-Host "PostgreSQL is available at localhost:$DatabasePort (database: $databaseName, user: $databaseUser)."
Write-Host "Logs: $ContainerRuntime compose logs -f"
