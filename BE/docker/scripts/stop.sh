#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
PROJECT_DIR="$(cd -- "${DOCKER_DIR}/.." && pwd)"
COMPOSE_FILE="${DOCKER_DIR}/compose.local.yml"
ENV_FILE="${DOCKER_DIR}/.env"

if ! command -v docker >/dev/null 2>&1; then
  echo "오류: Docker가 설치되어 있지 않습니다." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "오류: Docker가 실행 중이 아닙니다. Docker Desktop을 실행한 뒤 다시 시도해 주세요." >&2
  exit 1
fi

cd "${PROJECT_DIR}"

echo "클래스잇다 서버와 MySQL, Redis를 중지합니다."
docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" stop

echo
echo "중지 완료"
echo "컨테이너와 MySQL, Redis 데이터 볼륨은 삭제하지 않았습니다."
