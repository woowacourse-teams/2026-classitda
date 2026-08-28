-- 개발 배포 환경에서 Swagger로 API를 테스트하기 위한 전용 데이터다.
-- application-dev.yml이 시작할 때 reset-schema.sql과 schema.sql로 스키마를 초기화하므로
-- 고정 ID를 안전하게 사용한다. term과 permission은 schema.sql이 넣으므로 여기서 다루지 않는다.
-- local-data.sql과 시나리오는 같지만, 개발 배포에만 필요한 데이터가 아래에 더 붙는다.
-- 시간 기반 시나리오는 애플리케이션과 같은 한국 시간으로 계산한다.

SET time_zone = '+09:00';

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

INSERT INTO studio (id, owner_member_id, name, zonecode, road_address, jibun_address, building_name, detail_address,
                    phone_number, description, open_time, close_time, created_at, updated_at)
VALUES (1, 2, '클래스잇다 테스트 스튜디오', '06234', '서울 강남구 테헤란로 1', '서울 강남구 역삼동 823', NULL, '3층 301호',
        '+82200000001', 'local Swagger 테스트 전용 시설입니다.', '09:00:00', '22:00:00', CURRENT_TIMESTAMP(6), NULL);

INSERT INTO studio_policy (id, studio_id, reservation_close_minutes_before, free_cancel_minutes_before, waiting_offer_response_minutes, max_hold_days, created_at, updated_at)
VALUES (1, 1, 30, 120, 10, 7, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO class_type (id, studio_id, name, created_at, updated_at)
VALUES (1, 1, '리포머', CURRENT_TIMESTAMP(6), NULL),
       (2, 1, '체어', CURRENT_TIMESTAMP(6), NULL),
       (3, 1, '매트', CURRENT_TIMESTAMP(6), NULL);

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

INSERT INTO studio_membership (id, studio_id, member_id, studio_role_id, name, phone_number, status, joined_at, created_at, updated_at)
VALUES (1, 1, 1, 3, '김회원', '+821000000001', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL),
       (2, 1, 2, 1, '박대표', '+821000000002', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL),
       (3, 1, 3, 2, '이강사', '+821000000003', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL),
       (4, 1, 4, 3, '최회원', '+821000000004', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL),
       (5, 1, 5, 3, '정회원', '+821000000005', 'ACTIVE', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 DAY), CURRENT_TIMESTAMP(6), NULL);

INSERT INTO pass_product (id, studio_id, name, class_form, total_count, valid_period_amount, valid_period_unit, total_hold_days, is_active, created_at, updated_at)
VALUES (1, 1, '리포머 그룹 10회권', 'GROUP', 10, 12, 'MONTH', 7, TRUE, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO pass_product_class_type (id, pass_product_id, class_type_id, created_at, updated_at)
VALUES (1, 1, 1, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO member_pass_product (id, membership_id, pass_product_id, remaining_count, remaining_hold_days, status, started_at, expires_at, created_at, updated_at)
VALUES (42, 1, 1, 7, 7, 'ACTIVE', DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), CURRENT_TIMESTAMP(6), NULL),
       (43, 4, 1, 7, 7, 'ACTIVE', DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), CURRENT_TIMESTAMP(6), NULL),
       (44, 5, 1, 0, 7, 'ACTIVE', DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), CURRENT_TIMESTAMP(6), NULL);

-- 회원용 일별 목록은 회원 ID 1, 강사용 일별 목록은 강사 회원 ID 3으로 토큰을 발급한다.
-- 시설 ID 1과 '로컬 DB 초기화 날짜의 다음 날'로 조회한다.
-- 강사 회원 ID 3에게 회차 101~106은 mine=true, 대표가 담당하는 회차 107은 mine=false다.
-- 강사용 달력은 초기화 날짜의 전날부터 다음 날까지 조회한다.
-- 전날 완료 회차 108~109 중 회차 108만 강사 회원 ID 3의 수업이다.
-- 학생용 달력은 초기화 날짜의 5일 전부터 5일 후까지 조회한다.
-- 5일 전, 3일 전, 전날에는 attended=true다.
-- 다음 날, 3일 후, 5일 후에는 reserved=true이고 다음 날, 4일 후, 5일 후에는 waiting=true다.
-- 김회원(ID 1)은 다음 날 회차 117~120에서 제안·예약 취소·대기 취소·제안 만료를 비교한다.
-- 김회원(ID 1)은 2일 전 회차 121~122에서 RESERVED + 결석·출결 미기록을 비교한다.
-- 김회원(ID 1)은 다음 날 회차 117과 123에서 만석·여유 좌석이 있는 제안을 비교한다.
-- 정회원(ID 5)은 회차 123의 제안을 받았지만 모든 수강권 잔여 횟수가 0인 보조 경우다.
-- 신청 ID 29는 취소된 회차 106에 연결되어 학생 신청 상세에서 SESSION_CANCELED를 확인한다.
-- 강사용 전체 목록은 회차 124~132로 개인/그룹, 내/다른 강사, 예정/진행/완료/취소를 비교한다.
-- 회차 107과 124는 시작 시간이 같고 강사가 달라 (startAt, id) 커서의 동률 정렬을 확인할 수 있다.
-- 회차 125~129는 리포머/체어/매트와 개인/그룹 필터의 교차 결과를 확인한다.
-- 회차 125~128은 예약 0~2명, 대기 0~1명, 제안 포함 조합을 보여 준다.
-- 김회원의 다음 날 학생 Daily 기대 조합은 다음과 같다.
-- 101·105·118·119·120: NONE + RESERVABLE (118~120의 종료 이력은 활성 관계에서 제외)
-- 102: NONE + WAITLISTABLE, 103: RESERVED + availability=null, 104: WAITING + availability=null
-- 117: OFFERED + availability=null, reservedCount=2, remainingCapacity=0, waitingCount=1
-- 123: OFFERED + availability=null, reservedCount=2, remainingCapacity=1, waitingCount=0
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
       (116, 1, 3, '5일 후 대기 중 리포머', '같은 날짜의 예약 확정과 대기 중 표시 확인용 수업입니다.', 'GROUP', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), '18:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), '18:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (117, 1, 3, '제안 좌석까지 만석인 리포머', '예약 1명과 제안 1명이 좌석을 점유하고 대기 1명이 있는 수업입니다.', 'GROUP', 50, 2, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '18:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '18:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (118, 1, 3, '예약 취소 이력 리포머', '수강권이 연결된 예약을 취소한 경우입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '12:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '12:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (119, 1, 3, '대기 취소 이력 리포머', '수강권을 연결하지 않은 대기를 취소한 경우입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '13:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '13:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (120, 1, 3, '제안 만료 이력 리포머', '제안 기한이 지나 신청이 만료된 경우입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '15:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '15:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (121, 1, 3, '2일 전 결석 리포머', '예약 확정 후 결석으로 기록된 경우입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '14:00:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '14:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (122, 1, 3, '2일 전 출결 미기록 리포머', '지난 예약이지만 출결 결과를 기록하지 않은 경우입니다.', 'GROUP', 50, 5, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '16:00:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '16:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (123, 1, 3, '여유 좌석이 있는 제안 리포머', '제안 두 건이 좌석을 점유하고 한 좌석이 남은 경우입니다.', 'GROUP', 50, 3, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '16:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '16:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (124, 1, 3, '동시 시작 개인 리포머', '회차 107과 시작 시간이 같은 커서 동률 정렬 확인용 수업입니다.', 'INDIVIDUAL', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '12:30:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '13:20:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (125, 1, 2, '예약된 다른 강사 개인 체어', '다른 강사의 개인 수업과 예약 1명을 확인합니다.', 'INDIVIDUAL', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), '09:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), '09:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (126, 1, 3, '제안된 내 개인 매트', '제안이 좌석을 점유하는 개인 수업입니다.', 'INDIVIDUAL', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), '10:30:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), '11:20:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (127, 1, 3, '예약과 대기가 있는 그룹 매트', '예약과 제안 2명, 대기 1명을 표시하는 수업입니다.', 'GROUP', 50, 2, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), '12:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), '12:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (128, 1, 2, '예약이 없는 다른 강사 그룹 체어', '예약과 대기가 모두 0명인 다른 강사 수업입니다.', 'GROUP', 50, 6, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), '14:50:00'), NULL, CURRENT_TIMESTAMP(6), NULL),
       (129, 1, 3, '취소된 개인 매트', '개인 수업의 취소 상태를 확인합니다.', 'INDIVIDUAL', 50, 1, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 8 DAY), '09:00:00'), TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 8 DAY), '09:50:00'), CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL),
       (130, 1, 3, '진행 중인 그룹 리포머', '개발 DB 초기화 시점에 진행 중으로 표시되는 수업입니다.', 'GROUP', 50, 4, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 20 MINUTE), DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 30 MINUTE), NULL, CURRENT_TIMESTAMP(6), NULL),
       (131, 1, 3, '완료된 내 개인 체어', '내 개인 수업의 완료 상태를 확인합니다.', 'INDIVIDUAL', 50, 1, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 120 MINUTE), DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 70 MINUTE), NULL, CURRENT_TIMESTAMP(6), NULL),
       (132, 1, 2, '완료된 다른 강사 개인 리포머', '다른 강사의 개인 수업과 완료 상태를 확인합니다.', 'INDIVIDUAL', 50, 1, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 180 MINUTE), DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 130 MINUTE), NULL, CURRENT_TIMESTAMP(6), NULL);

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
       (116, 116, 1, CURRENT_TIMESTAMP(6), NULL),
       (117, 117, 1, CURRENT_TIMESTAMP(6), NULL),
       (118, 118, 1, CURRENT_TIMESTAMP(6), NULL),
       (119, 119, 1, CURRENT_TIMESTAMP(6), NULL),
       (120, 120, 1, CURRENT_TIMESTAMP(6), NULL),
       (121, 121, 1, CURRENT_TIMESTAMP(6), NULL),
       (122, 122, 1, CURRENT_TIMESTAMP(6), NULL),
       (123, 123, 1, CURRENT_TIMESTAMP(6), NULL),
       (124, 124, 1, CURRENT_TIMESTAMP(6), NULL),
       (125, 125, 2, CURRENT_TIMESTAMP(6), NULL),
       (126, 126, 3, CURRENT_TIMESTAMP(6), NULL),
       (127, 127, 3, CURRENT_TIMESTAMP(6), NULL),
       (128, 128, 2, CURRENT_TIMESTAMP(6), NULL),
       (129, 129, 3, CURRENT_TIMESTAMP(6), NULL),
       (130, 130, 1, CURRENT_TIMESTAMP(6), NULL),
       (131, 131, 2, CURRENT_TIMESTAMP(6), NULL),
       (132, 132, 1, CURRENT_TIMESTAMP(6), NULL);

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
       (18, 1, 116, NULL, 'WAITING', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (19, 1, 117, NULL, 'OFFERED', CURRENT_TIMESTAMP(6), DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 10 MINUTE), 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (20, 4, 117, 43, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (21, 5, 117, NULL, 'WAITING', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (22, 1, 118, 42, 'CANCELED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 1 DAY), CURRENT_TIMESTAMP(6)),
       (23, 1, 119, NULL, 'CANCELED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 1 DAY), CURRENT_TIMESTAMP(6)),
       (24, 1, 120, NULL, 'EXPIRED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 1 DAY), CURRENT_TIMESTAMP(6)),
       (25, 1, 121, 42, 'RESERVED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '14:00:00'), NULL, 'ABSENT', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '14:55:00'), CURRENT_TIMESTAMP(6), NULL),
       (26, 1, 122, 42, 'RESERVED', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '16:00:00'), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (27, 1, 123, NULL, 'OFFERED', CURRENT_TIMESTAMP(6), DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 10 MINUTE), 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (28, 5, 123, NULL, 'OFFERED', CURRENT_TIMESTAMP(6), DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 10 MINUTE), 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (29, 1, 106, 42, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (30, 1, 125, NULL, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (31, 1, 126, NULL, 'OFFERED', CURRENT_TIMESTAMP(6), DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 10 MINUTE), 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (32, 1, 127, NULL, 'RESERVED', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (33, 4, 127, NULL, 'OFFERED', CURRENT_TIMESTAMP(6), DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 10 MINUTE), 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (34, 5, 127, NULL, 'WAITING', CURRENT_TIMESTAMP(6), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (35, 4, 130, 43, 'RESERVED', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 1 DAY), NULL, 'NOT_RECORDED', NULL, CURRENT_TIMESTAMP(6), NULL),
       (36, 1, 131, NULL, 'RESERVED', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 1 DAY), NULL, 'ATTENDED', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 60 MINUTE), CURRENT_TIMESTAMP(6), NULL),
       (37, 4, 132, NULL, 'RESERVED', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 1 DAY), NULL, 'ABSENT', DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 120 MINUTE), CURRENT_TIMESTAMP(6), NULL);

-- 여기서부터 개발 배포 전용 데이터다.
-- 스웨거로 로그인 없이 API 를 눌러보기 위한 개발 전용 회원이다.
-- 위 시나리오 데이터가 member 와 auth_account 의 id 1~5 를 쓰므로 101 부터 사용한다.
INSERT INTO member (id, name, phone_number, profile_image_url, created_at, updated_at)
VALUES (101, '스웨거 테스트 회원', '01000000001', NULL, CURRENT_TIMESTAMP(6), NULL);

INSERT INTO auth_account (id, member_id, provider, provider_subject, provider_email, created_at, updated_at)
VALUES (101, 101, 'GOOGLE', 'dev-swagger-member-1', 'swagger-dev@example.com', CURRENT_TIMESTAMP(6), NULL);
