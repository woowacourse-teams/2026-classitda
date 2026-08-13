-- 스웨거로 로그인 없이 API를 눌러보기 위한 로컬 전용 회원이다.
INSERT INTO member (id, name, phone_number, profile_image_url, created_at, updated_at)
VALUES (1, '스웨거 테스트 회원', '+821000000001', NULL, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO auth_account (id, member_id, provider, provider_subject, provider_email, created_at, updated_at)
VALUES (1, 1, 'GOOGLE', 'local-swagger-member-1', 'swagger-local@example.com', CURRENT_TIMESTAMP(6), NULL);
