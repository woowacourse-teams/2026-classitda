#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
PROJECT_DIR="$(cd -- "${DOCKER_DIR}/.." && pwd)"
COMPOSE_FILE="${DOCKER_DIR}/compose.local.yml"
ENV_FILE="${DOCKER_DIR}/.env"
ENV_EXAMPLE="${DOCKER_DIR}/.env.example"

if ! command -v docker >/dev/null 2>&1; then
  echo "오류: Docker가 설치되어 있지 않습니다. Docker Desktop을 먼저 설치해 주세요." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "오류: Docker가 실행 중이 아닙니다. Docker Desktop을 실행한 뒤 다시 시도해 주세요." >&2
  exit 1
fi

if ! command -v openssl >/dev/null 2>&1; then
  echo "오류: openssl이 필요합니다. macOS와 대부분의 리눅스에는 기본 설치되어 있습니다." >&2
  exit 1
fi

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "환경변수 파일이 없어 ${ENV_EXAMPLE##*/} 을(를) 복사합니다."
  cp "${ENV_EXAMPLE}" "${ENV_FILE}"
fi

fill_if_empty() {
  local key="$1" value="$2" temp
  if grep -q "^${key}=$" "${ENV_FILE}"; then
    temp="$(mktemp)"
    sed "s|^${key}=$|${key}=${value}|" "${ENV_FILE}" > "${temp}"
    mv "${temp}" "${ENV_FILE}"
    echo "  ${key} 생성"
  fi
}

if grep -qE '^(AUTH_PHONE_KEY_HMAC_SECRET_BASE64|AUTH_JWT_PRIVATE_KEY_BASE64|AUTH_JWT_PUBLIC_KEY_BASE64)=$' "${ENV_FILE}"; then
  echo "비어 있는 키를 생성합니다."
  KEY_DIR="$(mktemp -d)"
  trap 'rm -rf "${KEY_DIR}"' EXIT

  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "${KEY_DIR}/private.pem" 2>/dev/null

  fill_if_empty "AUTH_PHONE_KEY_HMAC_SECRET_BASE64" "$(openssl rand -base64 32)"
  fill_if_empty "AUTH_JWT_PRIVATE_KEY_BASE64" \
    "$(openssl pkcs8 -topk8 -nocrypt -in "${KEY_DIR}/private.pem" -outform DER | base64 | tr -d '\n')"
  fill_if_empty "AUTH_JWT_PUBLIC_KEY_BASE64" \
    "$(openssl rsa -in "${KEY_DIR}/private.pem" -pubout -outform DER 2>/dev/null | base64 | tr -d '\n')"

  chmod 600 "${ENV_FILE}"
fi

cd "${PROJECT_DIR}"

echo "클래스잇다 서버와 MySQL, Redis를 시작합니다."
docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" up -d --build --wait --wait-timeout 180
docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" ps

FIXED_OTP="$(grep '^AUTH_SMS_LOCAL_FIXED_OTP=' "${ENV_FILE}" | cut -d= -f2)"

echo
echo "실행 완료"
echo "- 서버:    http://localhost:8080"
echo "- API 문서: http://localhost:8080/swagger-ui/index.html"
echo "- MySQL:   localhost:3306  (classitda / classitda)"
echo "- Redis:   localhost:6379"
echo
echo "휴대전화 인증번호는 항상 ${FIXED_OTP} 입니다. 실제 SMS는 발송되지 않습니다."
