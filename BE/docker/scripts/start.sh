#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
PROJECT_DIR="$(cd -- "${DOCKER_DIR}/.." && pwd)"
COMPOSE_FILE="${DOCKER_DIR}/compose.local.yml"

if ! command -v docker >/dev/null 2>&1; then
  echo "오류: Docker가 설치되어 있지 않습니다. Docker Desktop을 먼저 설치해 주세요." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "오류: Docker가 실행 중이 아닙니다. Docker Desktop을 실행한 뒤 다시 시도해 주세요." >&2
  exit 1
fi

cd "${PROJECT_DIR}"

echo "클래스잇다 서버와 MySQL을 시작합니다."
docker compose -f "${COMPOSE_FILE}" up -d --build
docker compose -f "${COMPOSE_FILE}" ps

echo
echo "실행 완료"
echo "- 서버: http://localhost:${LOCAL_APP_PORT:-8080}"
echo "- MySQL: localhost:${LOCAL_DB_PORT:-3306}"
