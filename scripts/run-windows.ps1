[CmdletBinding()]
param(
    [Alias('Runtime')]
    [string]$ContainerRuntime = 'auto',
    [ValidateRange(1, 65535)]
    [int]$Port = 8080,
    [switch]$Stop
)

$ErrorActionPreference = 'Stop'
$image = 'localhost/workshop-reiseapp:dev'
$container = 'workshop-reiseapp'
$repoRoot = Split-Path -Parent $PSScriptRoot

if ($ContainerRuntime -notin @('auto', 'podman', 'docker')) {
    throw "Invalid runtime '$ContainerRuntime'. Use auto, podman, or docker."
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

function Stop-ExistingContainer {
    Write-Host "Stopping existing container $container if present ..."
    & $ContainerRuntime stop -t 30 $container 2>$null | Out-Null
    & $ContainerRuntime rm -f $container 2>$null | Out-Null
}

if ($Stop) {
    Stop-ExistingContainer
    Write-Host "Container $container is stopped."
    exit 0
}

Require-Command 'java'
Require-Command 'mvn'

Write-Host "Checking Java, Maven, and $ContainerRuntime ..."
& java -version
& mvn -version

Stop-ExistingContainer
if (Test-NetConnection -ComputerName 127.0.0.1 -Port $Port -InformationLevel Quiet) {
    throw "Port $Port is already in use. Stop the service using it before starting again."
}

Push-Location $repoRoot
try {
    Write-Host 'Building and testing the project ...'
    & mvn clean verify
    if ($LASTEXITCODE -ne 0) { throw 'The Maven build failed.' }

    Write-Host "Building the container image with $ContainerRuntime ..."
    & $ContainerRuntime build -t $image ./service
    if ($LASTEXITCODE -ne 0) { throw 'The container build failed.' }

    Write-Host "Starting $container on host port $Port ..."
    & $ContainerRuntime run --rm --name $container -d -p "127.0.0.1:${Port}:8080" $image
    if ($LASTEXITCODE -ne 0) { throw 'The container could not be started.' }
} finally {
    Pop-Location
}

Write-Host "The service is running with $ContainerRuntime. Test: http://localhost:$Port/ping"
Write-Host "Logs: $ContainerRuntime logs -f $container"
