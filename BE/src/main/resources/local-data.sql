-- local 프로필에서 Swagger로 ClassSession API를 테스트하기 위한 전용 데이터다.
-- application-local.yml이 시작할 때 스키마를 초기화하므로 고정 ID를 안전하게 사용한다.

INSERT INTO member (id, name, phone_number, profile_image_url, created_at, updated_at)
VALUES (1, '김회원', '+821000000001', NULL, CURRENT_TIMESTAMP(6), NULL),
       (2, '박대표', '+821000000002', NULL, CURRENT_TIMESTAMP(6), NULL),
       (3, '이강사', '+821000000003', NULL, CURRENT_TIMESTAMP(6), NULL),
       (4, '최회원', '+821000000004', NULL, CURRENT_TIMESTAMP(6), NULL),
       (5, '정회원', '+821000000005', NULL, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO auth_account (id, member_id, provider, provider_subject, provider_email, created_at, updated_at)
VALUES (1, 1, 'GOOGLE', 'local-swagger-member-1', 'member1-local@example.com', CURRENT_TIMESTAMP(6), NULL),
       (2, 2, 'GOOGLE', 'local-swagger-member-2', 'owner-local@example.com', CURRENT_TIMESTAMP(6), NULL),
       (3, 3, 'GOOGLE', 'local-swagger-member-3', 'instructor-local@example.com', CURRENT_TIMESTAMP(6), NULL),
       (4, 4, 'GOOGLE', 'local-swagger-member-4', 'member4-local@example.com', CURRENT_TIMESTAMP(6), NULL),
       (5, 5, 'GOOGLE', 'local-swagger-member-5', 'member5-local@example.com', CURRENT_TIMESTAMP(6), NULL);

INSERT INTO studio (id, owner_member_id, name, address, phone_number, image_url, description, open_time, close_time, created_at, updated_at)
VALUES (1, 2, '클래스잇다 테스트 스튜디오', '서울시 강남구 테헤란로 1', '+82200000001', NULL, 'local Swagger 테스트 전용 시설입니다.', '09:00:00', '22:00:00', CURRENT_TIMESTAMP(6), NULL);

INSERT INTO studio_policy (id, studio_id, reservation_close_minutes_before, free_cancel_minutes_before, waiting_offer_response_minutes, max_hold_days, created_at, updated_at)
VALUES (1, 1, 30, 120, 10, 7, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO class_type (id, studio_id, name, created_at, updated_at)
VALUES (1, 1, '리포머', CURRENT_TIMESTAMP(6), NULL),
       (2, 1, '체어', CURRENT_TIMESTAMP(6), NULL);

INSERT INTO studio_role (id, studio_id, name, system_role, is_instructor, created_at, updated_at)
VALUES (1, 1, '대표 강사', 'OWNER', TRUE, CURRENT_TIMESTAMP(6), NULL),
       (2, 1, '일반 강사', 'INSTRUCTOR', TRUE, CURRENT_TIMESTAMP(6), NULL),
       (3, 1, '회원', 'STUDENT', FALSE, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO studio_role_permission (studio_role_id, permission_id, created_at, updated_at)
SELECT 1, permission.id, CURRENT_TIMESTAMP(6), NULL
FROM permission;

INSERT INTO studio_role_permission (studio_role_id, permission_id, created_at, updated_at)
SELECT 2, permission.id, CURRENT_TIMESTAMP(6), NULL
FROM permission
WHERE permission.code IN (
    'MEMBER_READ',
    'CLASS_TEMPLATE_MANAGE',
    'CLASS_SESSION_MANAGE_OWN',
    'RESERVATION_READ',
    'RESERVATION_MANAGE'
);

INSERT INTO studio_membership (id, studio_id, member_id, studio_role_id, name, status, joined_at, created_at, updated_at)
VALUES (1, 1, 1, 3, '김회원', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL),
       (2, 1, 2, 1, '박대표', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL),
       (3, 1, 3, 2, '이강사', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL),
       (4, 1, 4, 3, '최회원', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL),
       (5, 1, 5, 3, '정회원', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL);

INSERT INTO pass_product (id, studio_id, name, class_form, total_count, valid_period_amount, valid_period_unit, total_hold_days, is_active, created_at, updated_at)
VALUES (1, 1, '리포머 그룹 10회권', 'GROUP', 10, 12, 'MONTH', 7, TRUE, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO pass_product_class_type (id, pass_product_id, class_type_id, created_at, updated_at)
VALUES (1, 1, 1, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO member_pass_product (id, membership_id, pass_product_id, remaining_count, remaining_hold_days, status, started_at, expires_at, created_at, updated_at)
VALUES (42, 1, 1, 7, 7, 'ACTIVE', DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), CURRENT_TIMESTAMP(6), NULL),
       (43, 4, 1, 7, 7, 'ACTIVE', DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), CURRENT_TIMESTAMP(6), NULL),
       (44, 5, 1, 7, 7, 'ACTIVE', DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), CURRENT_TIMESTAMP(6), NULL);

-- 회원용 일별 목록은 회원 ID 1, 강사용 일별 목록은 강사 회원 ID 3으로 토큰을 발급한다.
-- 시설 ID 1과 '로컬 DB 초기화 날짜의 다음 날'로 조회한다.
-- 강사 회원 ID 3에게 회차 101~106은 mine=true, 대표가 담당하는 회차 107은 mine=false다.
-- 강사용 달력은 초기화 날짜의 전날부터 다음 날까지 조회한다.
-- 전날 완료 회차 108~109 중 회차 108만 강사 회원 ID 3의 수업이다.
-- 학생용 달력은 초기화 날짜의 5일 전부터 5일 후까지 조회한다.
-- 5일 전, 3일 전, 전날에는 attended=true다.
-- 다음 날, 3일 후, 5일 후에는 reserved=true이고 다음 날, 4일 후, 5일 후에는 waiting=true다.
INSERT INTO class_session (id, studio_id, instructor_membership_id, name, description, class_form, duration_minutes, capacity, start_at, end_at, canceled_at, created_at, updated_at)
VALUES (101, 1, 3, '리포머 베이직', '편한 복장과 개인 수건을 준비해 주세요.', 'GROUP', 50, 4, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '09:30:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '10:20:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (102, 1, 3, '리포머 대기 가능', '정원이 가득 차면 대기 신청할 수 있는 수업입니다.', 'GROUP', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '11:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '11:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (103, 1, 3, '리포머 예약 완료', '김회원이 예약한 수업입니다.', 'GROUP', 50, 6, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '14:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (104, 1, 3, '리포머 대기 중', '김회원이 대기 중인 수업입니다.', 'GROUP', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '17:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '17:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (105, 1, 3, '리포머 저녁 수업', '예약 단계는 시설 정책과 현재 시각으로 계산합니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '19:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '19:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (106, 1, 3, '리포머 취소 수업', '취소 상태 확인을 위한 수업입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '20:30:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '21:20:00'), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL),
       (107, 1, 2, '체어 입문', '선택한 리포머 수강권으로는 조회되지 않는 수업입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '12:30:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '13:20:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (108, 1, 3, '완료된 내 리포머 수업', '강사용 달력 완료 집계 확인용 수업입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '09:00:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '09:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (109, 1, 2, '완료된 다른 강사 수업', '강사용 달력 내 수업 집계 구분용 수업입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:30:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '11:20:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (110, 1, 3, '5일 전 출석 리포머', '학생용 달력 출석 표시 확인용 수업입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '10:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (111, 1, 3, '3일 전 출석 리포머', '학생용 달력 출석 표시 확인용 수업입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '18:00:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '18:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (112, 1, 3, '모레 예약 가능 리포머', '학생용 일별 목록 예약 가능 상태 확인용 수업입니다.', 'GROUP', 50, 4, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '15:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '15:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (113, 1, 3, '3일 후 예약 완료 리포머', '학생용 달력 예약 확정 표시 확인용 수업입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), '10:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (114, 1, 3, '4일 후 대기 중 리포머', '학생용 달력 대기 중 표시 확인용 수업입니다.', 'GROUP', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), '11:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), '11:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (115, 1, 3, '5일 후 예약 완료 리포머', '같은 날짜의 예약 확정과 대기 중 표시 확인용 수업입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), '10:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (116, 1, 3, '5일 후 대기 중 리포머', '같은 날짜의 예약 확정과 대기 중 표시 확인용 수업입니다.', 'GROUP', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), '18:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), '18:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO class_session_class_type (id, class_session_id, class_type_id, created_at, updated_at)
VALUES (101, 101, 1, CURRENT_TIMESTAMP(6), NULL),
       (102, 102, 1, CURRENT_TIMESTAMP(6), NULL),
       (103, 103, 1, CURRENT_TIMESTAMP(6), NULL),
       (104, 104, 1, CURRENT_TIMESTAMP(6), NULL),
       (105, 105, 1, CURRENT_TIMESTAMP(6), NULL),
       (106, 106, 1, CURRENT_TIMESTAMP(6), NULL),
       (107, 107, 2, CURRENT_TIMESTAMP(6), NULL),
       (108, 108, 1, CURRENT_TIMESTAMP(6), NULL),
       (109, 109, 1, CURRENT_TIMESTAMP(6), NULL),
       (110, 110, 1, CURRENT_TIMESTAMP(6), NULL),
       (111, 111, 1, CURRENT_TIMESTAMP(6), NULL),
       (112, 112, 1, CURRENT_TIMESTAMP(6), NULL),
       (113, 113, 1, CURRENT_TIMESTAMP(6), NULL),
       (114, 114, 1, CURRENT_TIMESTAMP(6), NULL),
       (115, 115, 1, CURRENT_TIMESTAMP(6), NULL),
       (116, 116, 1, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO reservation (id, membership_id, class_session_id, member_pass_product_id, status, reserved_at, canceled_at, created_at, updated_at)
VALUES (1, 4, 101, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (2, 5, 101, 44, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (3, 4, 102, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (4, 1, 103, 42, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (5, 4, 104, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (6, 1, 108, 42, 'ATTENDED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '09:00:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (7, 1, 110, 42, 'ATTENDED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '10:00:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (8, 1, 111, 42, 'ATTENDED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '18:00:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (9, 4, 112, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (10, 5, 112, 44, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (11, 1, 113, 42, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (12, 4, 114, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (13, 1, 115, 42, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL),
       (14, 4, 116, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO waiting (id, membership_id, class_session_id, sequence, status, offered_at, created_at, updated_at)
VALUES (1, 5, 102, 1, 'WAITING', NULL, CURRENT_TIMESTAMP(6), NULL),
       (2, 1, 104, 1, 'WAITING', NULL, CURRENT_TIMESTAMP(6), NULL),
       (3, 1, 114, 1, 'WAITING', NULL, CURRENT_TIMESTAMP(6), NULL),
       (4, 1, 116, 1, 'WAITING', NULL, CURRENT_TIMESTAMP(6), NULL);

-- 통합 모델 전환 전까지 기존 조회가 사용하는 reservation/waiting과 같은 상태를 병행해 둔다.
INSERT INTO class_session_enrollment (
    id,
    membership_id,
    class_session_id,
    member_pass_product_id,
    enrollment_status,
    enrollment_status_changed_at,
    offer_expires_at,
    attendance_result,
    attendance_recorded_at,
    created_at,
    updated_at
)
VALUES (1, 4, 101, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (2, 5, 101, 44, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (3, 4, 102, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (4, 1, 103, 42, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (5, 4, 104, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (6, 1, 108, 42, 'RESERVED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '09:00:00'), NULL, 'ATTENDED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '09:55:00'), CURRENT_TIMESTAMP(6), NULL),
       (7, 1, 110, 42, 'RESERVED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '10:00:00'), NULL, 'ATTENDED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '10:55:00'), CURRENT_TIMESTAMP(6), NULL),
       (8, 1, 111, 42, 'RESERVED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '18:00:00'), NULL, 'ATTENDED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '18:55:00'), CURRENT_TIMESTAMP(6), NULL),
       (9, 4, 112, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (10, 5, 112, 44, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (11, 1, 113, 42, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (12, 4, 114, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (13, 1, 115, 42, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (14, 4, 116, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (15, 5, 102, NULL, 'WAITING', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (16, 1, 104, NULL, 'WAITING', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (17, 1, 114, NULL, 'WAITING', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (18, 1, 116, NULL, 'WAITING', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL);
