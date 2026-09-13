#!/usr/bin/env bash
set -Eeuo pipefail

usage() {
  cat <<'EOF'
Usage: bash scripts/run-unix.sh [options]

Options:
  --runtime podman|docker  Select the container runtime.
  --port PORT              Host port (default: 8080).
  --stop                   Stop the service container and exit.
  --help                   Show this help.

The default action builds, tests, and starts the service in the background.
EOF
}

runtime=""
port=8080
stop=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    --runtime)
      [[ $# -ge 2 ]] || { echo "--runtime requires podman or docker" >&2; exit 2; }
      runtime="$2"
      shift 2
      ;;
    --port)
      [[ $# -ge 2 && "$2" =~ ^[0-9]+$ && "$2" -ge 1 && "$2" -le 65535 ]] || {
        echo "--port requires a number between 1 and 65535" >&2
        exit 2
      }
      port="$2"
      shift 2
      ;;
    --stop)
      stop=true
      shift
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if [[ -z "$runtime" ]]; then
  runtimes=()
  command -v podman >/dev/null 2>&1 && runtimes+=(podman)
  command -v docker >/dev/null 2>&1 && runtimes+=(docker)

  case "${#runtimes[@]}" in
    0)
      echo "Neither podman nor docker was found. Run the installation script first." >&2
      exit 1
      ;;
    1)
      runtime="${runtimes[0]}"
      ;;
    *)
      echo "Choose a container runtime:"
      select choice in "${runtimes[@]}"; do
        [[ -n "${choice:-}" ]] && { runtime="$choice"; break; }
        echo "Enter a number from the list."
      done
      ;;
  esac
fi

case "$runtime" in
  podman|docker) ;;
  *) echo "Invalid runtime '$runtime'. Use podman or docker." >&2; exit 2 ;;
esac

command -v "$runtime" >/dev/null 2>&1 || { echo "$runtime is not installed or not available in PATH." >&2; exit 1; }
"$runtime" info >/dev/null

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"
image="localhost/workshop-reiseapp:dev"
container="workshop-reiseapp"

stop_existing_container() {
  echo "Stopping existing container $container if present ..."
  "$runtime" stop -t 30 "$container" >/dev/null 2>&1 || true
  "$runtime" rm -f "$container" >/dev/null 2>&1 || true
}

if [[ "$stop" == true ]]; then
  stop_existing_container
  echo "Container $container is stopped."
  exit 0
fi

command -v java >/dev/null 2>&1 || { echo "Java is not installed or not available in PATH." >&2; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "Maven is not installed or not available in PATH." >&2; exit 1; }

echo "Checking Java, Maven, and $runtime ..."
java -version
mvn -version

stop_existing_container
if command -v nc >/dev/null 2>&1 && nc -z 127.0.0.1 "$port" >/dev/null 2>&1; then
  echo "Port $port is already in use. Stop the service using it before starting again." >&2
  exit 1
fi

echo "Building and testing the project ..."
mvn clean verify

echo "Building the container image with $runtime ..."
"$runtime" build -t "$image" ./service

echo "Starting $container on host port $port ..."
"$runtime" run --rm --name "$container" -d -p "127.0.0.1:${port}:8080" "$image"

echo "The service is running with $runtime. Test: http://localhost:$port/ping"
echo "Logs: $runtime logs -f $container"
