#!/usr/bin/env bash
set -euo pipefail

maven_version=3.9.16
runtime=''
plan=false
usage() { echo "Bruk: bash $0 [--runtime podman|docker] [--plan]"; }
while (($#)); do
    case "$1" in
        --runtime) [[ $# -ge 2 ]] || { usage; exit 2; }; runtime=$2; shift 2 ;;
        --plan) plan=true; shift ;;
        --help|-h) usage; exit 0 ;;
        *) usage; exit 2 ;;
    esac
done
if [[ -z "$runtime" ]]; then
    while :; do
        read -r -p 'Velg containerverktøy: 1 = Podman (anbefalt), 2 = Docker: ' choice || exit 2
        case "$choice" in
            1|podman) runtime=podman; break ;;
            2|docker) runtime=docker; break ;;
            *) echo 'Skriv 1 eller 2.' ;;
        esac
    done
fi
[[ "$runtime" == podman || "$runtime" == docker ]] || { usage; exit 2; }

platform=$(uname -s)
case "$platform" in
    Darwin) jdk_os=mac; distro=macos ;;
    Linux)
        jdk_os=linux
        [[ -r /etc/os-release ]] || { echo 'Mangler /etc/os-release.' >&2; exit 1; }
        . /etc/os-release
        distro=$ID
        case "$distro" in
            ubuntu|debian|fedora) ;;
            *) echo "Ustøttet Linux-distribusjon: $distro. Støtter Ubuntu, Debian og Fedora." >&2; exit 1 ;;
        esac ;;
    *) echo "Ustøttet plattform: $platform. Bruk Windows-skriptet på Windows." >&2; exit 1 ;;
esac
case "$(uname -m)" in
    x86_64) jdk_arch=x64 ;;
    arm64|aarch64) jdk_arch=aarch64 ;;
    *) echo 'Støtter bare x86_64 og ARM64.' >&2; exit 1 ;;
esac
echo "Plan ($distro/$jdk_arch): Temurin JDK 25, Maven $maven_version og $runtime med Compose."
echo 'Java og Maven installeres under ~/.local/share/workshop-reiseapp/tools.'
echo 'Containerverktøy bruker pakkebehandleren. Eksisterende containerinstallasjon beholdes.'
echo 'Miljøfil opprettes; shell-profilen din endres ikke. Ingen containere startes.'
$plan && exit 0
[[ $EUID -ne 0 ]] || { echo 'Kjør som vanlig bruker; skriptet bruker sudo ved behov.' >&2; exit 1; }

if [[ "$distro" == macos ]]; then
    command -v brew >/dev/null || { echo 'Installer Homebrew fra https://brew.sh og kjør igjen.' >&2; exit 1; }
    brew install jq
else
    command -v sudo >/dev/null || { echo 'sudo må være installert.' >&2; exit 1; }
    if [[ "$distro" == fedora ]]; then
        sudo dnf install -y curl jq tar gzip ca-certificates
    else
        sudo apt-get update
        sudo apt-get install -y curl jq tar gzip ca-certificates
    fi
fi

tools_root="$HOME/.local/share/workshop-reiseapp/tools"
mkdir -p "$tools_root/downloads"
download_dir="$tools_root/downloads"
fetch() { curl --fail --location --retry 3 --proto '=https' --tlsv1.2 "$1" -o "$2"; }
hash_file() {
    if command -v sha256sum >/dev/null; then
        if [[ "$1" == 256 ]]; then sha256sum "$2"; else sha512sum "$2"; fi
    else
        shasum -a "$1" "$2"
    fi | awk '{print $1}'
}

# Pin the major LTS version; use the newest GA patch published for this platform.
fetch "https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=$jdk_arch&image_type=jdk&os=$jdk_os&vendor=eclipse" "$download_dir/jdk.json"
jdk_url=$(jq -er '.[0].binary.package.link' "$download_dir/jdk.json")
jdk_hash=$(jq -er '.[0].binary.package.checksum' "$download_dir/jdk.json")
[[ "$jdk_hash" =~ ^[a-fA-F0-9]{64}$ ]] || { echo 'Ugyldig JDK-sjekksum.' >&2; exit 1; }
jdk_dir="$tools_root/temurin-25-$jdk_hash"
if [[ ! -f "$jdk_dir/.installed" ]]; then
    fetch "$jdk_url" "$download_dir/jdk.tar.gz"
    [[ "$(hash_file 256 "$download_dir/jdk.tar.gz")" == "$jdk_hash" ]] || { echo 'JDK SHA-256 stemmer ikke.' >&2; exit 1; }
    mkdir -p "$jdk_dir"
    tar -xzf "$download_dir/jdk.tar.gz" -C "$jdk_dir" --strip-components=1
    touch "$jdk_dir/.installed"
fi
export JAVA_HOME="$jdk_dir"
[[ "$platform" != Darwin ]] || export JAVA_HOME="$jdk_dir/Contents/Home"

maven_dir="$tools_root/apache-maven-$maven_version"
if [[ ! -f "$maven_dir/.installed" ]]; then
    maven_url="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$maven_version/apache-maven-$maven_version-bin.tar.gz"
    fetch "$maven_url" "$download_dir/maven.tar.gz"
    fetch "$maven_url.sha512" "$download_dir/maven.sha512"
    maven_hash=$(awk '{print $1; exit}' "$download_dir/maven.sha512")
    [[ "$maven_hash" =~ ^[a-fA-F0-9]{128}$ ]] || { echo 'Ugyldig Maven-sjekksum.' >&2; exit 1; }
    [[ "$(hash_file 512 "$download_dir/maven.tar.gz")" == "$maven_hash" ]] || { echo 'Maven SHA-512 stemmer ikke.' >&2; exit 1; }
    mkdir -p "$maven_dir"
    tar -xzf "$download_dir/maven.tar.gz" -C "$maven_dir" --strip-components=1
    touch "$maven_dir/.installed"
fi
export PATH="$JAVA_HOME/bin:$maven_dir/bin:$PATH"
env_file="$tools_root/env.sh"
printf 'export JAVA_HOME=%q\nexport PATH=%q:%q:"$PATH"\n' "$JAVA_HOME" "$JAVA_HOME/bin" "$maven_dir/bin" > "$env_file"
java -version
mvn --version

if [[ "$distro" == macos ]]; then
    if [[ "$runtime" == podman ]]; then
        command -v podman >/dev/null || brew install podman
        command -v podman-compose >/dev/null || brew install podman-compose
    else
        command -v docker >/dev/null || brew install --cask docker-desktop
    fi
elif [[ "$runtime" == podman ]]; then
    podman_packages=()
    command -v podman >/dev/null || podman_packages+=(podman)
    command -v podman-compose >/dev/null || podman_packages+=(podman-compose)
    if [[ "$distro" == fedora ]]; then
        if ((${#podman_packages[@]})); then sudo dnf install -y "${podman_packages[@]}"; fi
    else
        if ((${#podman_packages[@]})); then sudo apt-get install -y "${podman_packages[@]}" uidmap slirp4netns; fi
    fi
elif ! command -v docker >/dev/null; then
    # Use Docker's official stable repository, without removing conflicting packages.
    if [[ "$distro" == fedora ]]; then
        fetch 'https://download.docker.com/linux/fedora/docker-ce.repo' "$download_dir/docker-ce.repo"
        sudo install -m 644 "$download_dir/docker-ce.repo" /etc/yum.repos.d/docker-ce.repo
        sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    else
        : "${VERSION_CODENAME:?Distribusjonen mangler VERSION_CODENAME}"
        fetch "https://download.docker.com/linux/$distro/gpg" "$download_dir/docker.asc"
        sudo install -d -m 755 /etc/apt/keyrings
        sudo install -m 644 "$download_dir/docker.asc" /etc/apt/keyrings/docker.asc
        printf 'deb [arch=%s signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/%s %s stable\n' \
            "$(dpkg --print-architecture)" "$distro" "$VERSION_CODENAME" > "$download_dir/docker.list"
        sudo install -m 644 "$download_dir/docker.list" /etc/apt/sources.list.d/workshop-docker.list
        sudo apt-get update
        sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    fi
fi

echo ''
echo 'Pakker installert. Aktiver Java/Maven i terminalen (og i nye terminaler) med:'
printf 'source %q\n' "$env_file"
if [[ "$runtime" == podman ]]; then
    podman --version
    podman-compose --version
    if [[ "$distro" == macos ]]; then
        echo 'Kontroller podman machine list. Hvis ingen maskin finnes: podman machine init --rootful=false'
        echo 'Start med podman machine start <maskinnavn>. Eksisterende maskiner endres ikke.'
    fi
    echo 'Kontroller podman info og podman compose version før oppstart.'
else
    docker --version
    if ! docker compose version; then
        # Add the official standalone plugin without replacing an existing Docker installation.
        compose_version=5.5.1
        compose_arch=$(uname -m)
        [[ "$compose_arch" != arm64 ]] || compose_arch=aarch64
        compose_os=$(printf '%s' "$platform" | tr '[:upper:]' '[:lower:]')
        compose_url="https://github.com/docker/compose/releases/download/v$compose_version/docker-compose-$compose_os-$compose_arch"
        fetch "$compose_url" "$download_dir/docker-compose"
        fetch "$compose_url.sha256" "$download_dir/compose.sha256"
        compose_hash=$(awk '{print $1; exit}' "$download_dir/compose.sha256")
        [[ "$compose_hash" =~ ^[a-fA-F0-9]{64}$ ]] || { echo 'Ugyldig Compose-sjekksum.' >&2; exit 1; }
        [[ "$(hash_file 256 "$download_dir/docker-compose")" == "$compose_hash" ]] || { echo 'Compose SHA-256 stemmer ikke.' >&2; exit 1; }
        plugin_dir="${DOCKER_CONFIG:-$HOME/.docker}/cli-plugins"
        mkdir -p "$plugin_dir"
        if [[ -e "$plugin_dir/docker-compose" ]]; then
            echo "En eksisterende Compose-plugin i $plugin_dir virker ikke. Rett denne og kjør igjen." >&2
            exit 1
        fi
        install -m 755 "$download_dir/docker-compose" "$plugin_dir/docker-compose"
        docker compose version
    fi
    if [[ "$distro" == macos ]]; then
        echo 'Start Docker Desktop og fullfør førstegangsoppsettet.'
    else
        echo 'Start Docker ved behov: sudo systemctl start docker'
        echo 'Bruk sudo docker ved behov. En Compose-plugin i din hjemmemappe krever Docker-tilgang som din bruker.'
        echo 'Se https://docs.docker.com/engine/install/linux-postinstall/ for tilgang uten sudo.'
    fi
fi
echo 'Fra prosjektroten: mvn clean verify'
