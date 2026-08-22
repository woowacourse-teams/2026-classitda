-- 개발 배포 환경 전용 시드 데이터.
-- dev 프로파일은 ddl-auto: create 로 테이블을 만들므로 schema.sql 의 시드가 실행되지 않는다.

INSERT INTO term (code, title, url, is_required, version, created_at, updated_at)
VALUES ('SERVICE_TERMS', '서비스 이용약관', 'https://example.invalid/terms/service-v1', TRUE, 1,
        CURRENT_TIMESTAMP(6), NULL),
       ('PRIVACY_POLICY', '개인정보 처리방침', 'https://example.invalid/terms/privacy-v1', TRUE, 1,
        CURRENT_TIMESTAMP(6), NULL),
       ('MARKETING_CONSENT', '마케팅 정보 수신 동의', 'https://example.invalid/terms/marketing-v1', FALSE, 1,
        CURRENT_TIMESTAMP(6), NULL);

-- 스웨거로 로그인 없이 API 를 눌러보기 위한 개발 전용 회원이다.
INSERT INTO member (id, name, phone_number, profile_image_url, created_at, updated_at)
VALUES (1, '스웨거 테스트 회원', '01000000001', NULL, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO auth_account (id, member_id, provider, provider_subject, provider_email, created_at, updated_at)
VALUES (1, 1, 'GOOGLE', 'dev-swagger-member-1', 'swagger-dev@example.com', CURRENT_TIMESTAMP(6), NULL);

-- 권한 코드. PermissionCode enum과 1:1로 대응한다.
-- enum에 상수를 추가하면 이 파일에도 함께 추가해야 한다.
INSERT INTO permission (code, created_at)
VALUES ('STUDIO_UPDATE', NOW(6)),
       ('POLICY_MANAGE', NOW(6)),
       ('ROOM_MANAGE', NOW(6)),
       ('ROLE_MANAGE', NOW(6)),
       ('MEMBER_READ', NOW(6)),
       ('MEMBER_INVITE', NOW(6)),
       ('MEMBER_MANAGE', NOW(6)),
       ('CLASS_TYPE_MANAGE', NOW(6)),
       ('CLASS_TEMPLATE_MANAGE', NOW(6)),
       ('CLASS_SESSION_MANAGE_OWN', NOW(6)),
       ('CLASS_SESSION_MANAGE_ALL', NOW(6)),
       ('RESERVATION_READ', NOW(6)),
       ('RESERVATION_MANAGE', NOW(6)),
       ('PASS_PRODUCT_MANAGE', NOW(6)),
       ('PASS_MEMBER_MANAGE', NOW(6)),
       ('NOTICE_MANAGE', NOW(6));
