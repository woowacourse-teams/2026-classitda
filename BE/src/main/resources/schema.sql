CREATE TABLE member
(
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    name                    VARCHAR(50)  NOT NULL,
    phone_number            VARCHAR(20)  NULL,
    profile_image_url       VARCHAR(500) NULL,
    withdrawal_requested_at DATETIME(6)  NULL,
    cleanup_scheduled_at    DATETIME(6)  NULL,
    cleaned_up_at           DATETIME(6)  NULL,
    created_at              DATETIME(6)  NOT NULL,
    updated_at              DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_phone_number (phone_number),
    KEY idx_member_cleanup (cleaned_up_at, cleanup_scheduled_at, id),
    CONSTRAINT chk_member_phone_by_cleanup
        CHECK (
            (cleaned_up_at IS NULL AND phone_number IS NOT NULL)
            OR (cleaned_up_at IS NOT NULL AND phone_number IS NULL)
        )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE auth_account
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    member_id        BIGINT       NOT NULL,
    provider         VARCHAR(20)  NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    provider_email   VARCHAR(254) NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_account_member (member_id),
    UNIQUE KEY uk_auth_account_provider_subject (provider, provider_subject),
    CONSTRAINT fk_auth_account_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE term
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    code        VARCHAR(50) NOT NULL,
    title       VARCHAR(100) NOT NULL,
    url         VARCHAR(500) NOT NULL,
    is_required BOOLEAN     NOT NULL,
    version     INT         NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_term_code_version (code, version)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


INSERT INTO term (code, title, url, is_required, version, created_at, updated_at)
VALUES ('SERVICE_TERMS', '서비스 이용약관', 'https://example.invalid/terms/service-v1', TRUE, 1, CURRENT_TIMESTAMP(6), NULL),
       ('PRIVACY_POLICY', '개인정보 처리방침', 'https://example.invalid/terms/privacy-v1', TRUE, 1, CURRENT_TIMESTAMP(6), NULL),
       ('MARKETING_CONSENT', '마케팅 정보 수신 동의', 'https://example.invalid/terms/marketing-v1', FALSE, 1, CURRENT_TIMESTAMP(6), NULL);


CREATE TABLE member_term_agreement
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    term_id    BIGINT      NOT NULL,
    agreed     BOOLEAN     NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_agreement_member_term (member_id, term_id),
    CONSTRAINT fk_agreement_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_agreement_term FOREIGN KEY (term_id) REFERENCES term (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE studio
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    owner_member_id  BIGINT       NOT NULL,
    name             VARCHAR(50)  NOT NULL,
    zonecode         VARCHAR(5)   NOT NULL,
    road_address     VARCHAR(255) NOT NULL,
    jibun_address    VARCHAR(255) NULL,
    building_name    VARCHAR(100) NULL,
    detail_address   VARCHAR(100) NULL,
    phone_number     VARCHAR(20)  NULL,
    image_object_key VARCHAR(255) NULL,
    description      TEXT         NULL,
    open_time        TIME         NOT NULL,
    close_time       TIME         NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_studio_image_key (image_object_key),
    CONSTRAINT fk_studio_owner FOREIGN KEY (owner_member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE room
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id  BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_studio_name (studio_id, name),
    CONSTRAINT fk_room_studio FOREIGN KEY (studio_id) REFERENCES studio (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE class_type
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id  BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_class_type_studio_name (studio_id, name),
    CONSTRAINT fk_class_type_studio FOREIGN KEY (studio_id) REFERENCES studio (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE studio_policy
(
    id                               BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id                        BIGINT      NOT NULL,
    reservation_close_minutes_before INT         NOT NULL,
    free_cancel_minutes_before       INT         NOT NULL,
    waiting_offer_response_minutes   INT         NOT NULL,
    max_hold_days                    INT         NOT NULL DEFAULT 0,
    created_at                       DATETIME(6) NOT NULL,
    updated_at                       DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_policy_studio (studio_id),
    CONSTRAINT fk_policy_studio FOREIGN KEY (studio_id) REFERENCES studio (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE permission
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    code       VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE studio_role
(
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id          BIGINT      NOT NULL,
    name               VARCHAR(50) NOT NULL,
    system_role        VARCHAR(20) NULL,
    is_instructor      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at         DATETIME(6) NOT NULL,
    updated_at         DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_studio_name (studio_id, name),
    UNIQUE KEY uk_role_studio_system (studio_id, system_role),
    CONSTRAINT fk_role_studio FOREIGN KEY (studio_id) REFERENCES studio (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE studio_role_permission
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    studio_role_id BIGINT      NOT NULL,
    permission_id  BIGINT      NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_studio_role_permission (studio_role_id, permission_id),
    CONSTRAINT fk_studio_role_permission_role FOREIGN KEY (studio_role_id) REFERENCES studio_role (id),
    CONSTRAINT fk_studio_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permission (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE studio_membership
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id       BIGINT      NOT NULL,
    member_id       BIGINT      NOT NULL,
    studio_role_id  BIGINT      NOT NULL,
    name            VARCHAR(50) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    joined_at       DATETIME(6) NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_membership_studio_member (studio_id, member_id),
    CONSTRAINT fk_membership_studio FOREIGN KEY (studio_id) REFERENCES studio (id),
    CONSTRAINT fk_membership_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_membership_role FOREIGN KEY (studio_role_id) REFERENCES studio_role (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE class_template
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    studio_id        BIGINT       NOT NULL,
    name             VARCHAR(100) NOT NULL,
    description      TEXT         NULL,
    class_form       VARCHAR(20)  NOT NULL,
    duration_minutes INT          NOT NULL,
    start_time       TIME         NOT NULL,
    capacity         INT          NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_template_studio FOREIGN KEY (studio_id) REFERENCES studio (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE class_template_recurring_day
(
    class_template_id BIGINT      NOT NULL,
    day_of_week       VARCHAR(10) NOT NULL,
    PRIMARY KEY (class_template_id, day_of_week),
    CONSTRAINT fk_template_recurring_day_template
        FOREIGN KEY (class_template_id) REFERENCES class_template (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE class_template_class_type
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    class_template_id BIGINT      NOT NULL,
    class_type_id     BIGINT      NOT NULL,
    created_at        DATETIME(6) NOT NULL,
    updated_at        DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_class_type (class_template_id),
    CONSTRAINT fk_template_class_type_template
        FOREIGN KEY (class_template_id) REFERENCES class_template (id) ON DELETE CASCADE,
    CONSTRAINT fk_template_class_type_type
        FOREIGN KEY (class_type_id) REFERENCES class_type (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE class_session
(
    id                       BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id                BIGINT      NOT NULL,
    instructor_membership_id BIGINT      NOT NULL,
    name                     VARCHAR(100) NOT NULL,
    description              TEXT        NULL,
    class_form               VARCHAR(20) NOT NULL,
    duration_minutes         INT         NOT NULL,
    capacity                 INT         NOT NULL,
    start_at                 DATETIME(6) NOT NULL,
    end_at                   DATETIME(6) NOT NULL,
    canceled_at              DATETIME(6) NULL,
    active_flag              TINYINT GENERATED ALWAYS AS (IF(canceled_at IS NOT NULL, NULL, 1)) STORED,
    created_at               DATETIME(6) NOT NULL,
    updated_at               DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_instructor_active (instructor_membership_id, start_at, active_flag),
    KEY idx_session_studio_start (studio_id, start_at, id),
    CONSTRAINT fk_session_studio FOREIGN KEY (studio_id) REFERENCES studio (id),
    CONSTRAINT fk_session_instructor FOREIGN KEY (instructor_membership_id) REFERENCES studio_membership (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE class_session_class_type
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    class_session_id BIGINT      NOT NULL,
    class_type_id    BIGINT      NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_class_type_session (class_session_id),
    CONSTRAINT fk_session_class_type_session
        FOREIGN KEY (class_session_id) REFERENCES class_session (id) ON DELETE CASCADE,
    CONSTRAINT fk_session_class_type_type
        FOREIGN KEY (class_type_id) REFERENCES class_type (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE pass_product
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    studio_id           BIGINT       NOT NULL,
    name                VARCHAR(100) NOT NULL,
    class_form          VARCHAR(20)  NOT NULL,
    total_count         INT          NULL,
    valid_period_amount INT          NULL,
    valid_period_unit   VARCHAR(10)  NULL,
    total_hold_days     INT          NOT NULL DEFAULT 0,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6)  NOT NULL,
    updated_at          DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pass_product_studio FOREIGN KEY (studio_id) REFERENCES studio (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE pass_product_class_type
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    pass_product_id BIGINT      NOT NULL,
    class_type_id   BIGINT      NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pass_product_class_type (pass_product_id, class_type_id),
    CONSTRAINT fk_pass_product_class_type_pass_product FOREIGN KEY (pass_product_id) REFERENCES pass_product (id),
    CONSTRAINT fk_pass_product_class_type_class_type FOREIGN KEY (class_type_id) REFERENCES class_type (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE member_pass_product
(
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    membership_id       BIGINT      NOT NULL,
    pass_product_id     BIGINT      NOT NULL,
    remaining_count     INT         NULL,
    remaining_hold_days INT         NOT NULL DEFAULT 0,
    status              VARCHAR(20) NOT NULL,
    started_at          DATE        NOT NULL,
    expires_at          DATE        NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_member_pass_product_membership FOREIGN KEY (membership_id) REFERENCES studio_membership (id),
    CONSTRAINT fk_member_pass_product_pass_product FOREIGN KEY (pass_product_id) REFERENCES pass_product (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE class_session_enrollment
(
    id                           BIGINT      NOT NULL AUTO_INCREMENT,
    membership_id                BIGINT      NOT NULL,
    class_session_id             BIGINT      NOT NULL,
    member_pass_product_id       BIGINT      NULL,
    enrollment_status            VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    enrollment_status_changed_at DATETIME(6) NOT NULL,
    offer_expires_at              DATETIME(6) NULL,
    attendance_result             VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    attendance_recorded_at        DATETIME(6) NULL,
    active_flag                   TINYINT GENERATED ALWAYS AS (
        IF(enrollment_status IN ('WAITING', 'OFFERED', 'RESERVED'), 1, NULL)
        ) STORED,
    created_at                    DATETIME(6) NOT NULL,
    updated_at                    DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_enrollment_active (class_session_id, membership_id, active_flag),
    KEY idx_enrollment_queue (class_session_id, enrollment_status, enrollment_status_changed_at, id),
    KEY idx_enrollment_offer_expiry (enrollment_status, offer_expires_at, id),
    KEY idx_enrollment_member_calendar (membership_id, enrollment_status, class_session_id),
    CONSTRAINT fk_enrollment_membership
        FOREIGN KEY (membership_id) REFERENCES studio_membership (id),
    CONSTRAINT fk_enrollment_session
        FOREIGN KEY (class_session_id) REFERENCES class_session (id),
    CONSTRAINT fk_enrollment_member_pass_product
        FOREIGN KEY (member_pass_product_id) REFERENCES member_pass_product (id),
    CONSTRAINT chk_enrollment_status
        CHECK (enrollment_status IN ('WAITING', 'OFFERED', 'RESERVED', 'CANCELED', 'EXPIRED')),
    CONSTRAINT chk_enrollment_offer
        CHECK (
            (enrollment_status = 'OFFERED'
                AND offer_expires_at IS NOT NULL
                AND offer_expires_at > enrollment_status_changed_at)
            OR (enrollment_status <> 'OFFERED' AND offer_expires_at IS NULL)
        ),
    -- ADR-0015: 'RESERVED' 는 원래 member_pass_product_id 가 필수다.
    -- 수강권 발급·차감 기능이 없는 MVP 기간에만 해제한 상태이며, 수강권 도입 시 아래로 복원한다.
    --     OR (enrollment_status = 'RESERVED' AND member_pass_product_id IS NOT NULL)
    --     OR enrollment_status = 'CANCELED'
    CONSTRAINT chk_enrollment_pass
        CHECK (
            (enrollment_status IN ('WAITING', 'OFFERED', 'EXPIRED')
                AND member_pass_product_id IS NULL)
            OR enrollment_status IN ('RESERVED', 'CANCELED')
        ),
    CONSTRAINT chk_enrollment_attendance_result
        CHECK (attendance_result IN ('NOT_RECORDED', 'ATTENDED', 'ABSENT')),
    CONSTRAINT chk_enrollment_attendance_recorded_at
        CHECK (
            (attendance_result = 'NOT_RECORDED' AND attendance_recorded_at IS NULL)
            OR (attendance_result IN ('ATTENDED', 'ABSENT')
                AND attendance_recorded_at IS NOT NULL)
        ),
    CONSTRAINT chk_enrollment_attendance_status
        CHECK (attendance_result = 'NOT_RECORDED' OR enrollment_status = 'RESERVED')
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE notice
(
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    studio_id            BIGINT       NOT NULL,
    author_membership_id BIGINT       NOT NULL,
    title                VARCHAR(200) NOT NULL,
    content              TEXT         NOT NULL,
    image_url            VARCHAR(500) NULL,
    created_at           DATETIME(6)  NOT NULL,
    updated_at           DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notice_studio FOREIGN KEY (studio_id) REFERENCES studio (id),
    CONSTRAINT fk_notice_author FOREIGN KEY (author_membership_id) REFERENCES studio_membership (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

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
