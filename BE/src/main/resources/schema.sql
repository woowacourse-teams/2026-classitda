CREATE TABLE member
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    name              VARCHAR(50)  NOT NULL,
    phone_number      VARCHAR(20)  NOT NULL,
    profile_image_url VARCHAR(500) NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_phone_number (phone_number)
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
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    owner_member_id BIGINT       NOT NULL,
    name            VARCHAR(50)  NOT NULL,
    address         VARCHAR(255) NULL,
    phone_number    VARCHAR(20)  NULL,
    image_url       VARCHAR(500) NULL,
    description     TEXT         NULL,
    open_time       TIME         NOT NULL,
    close_time      TIME         NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NULL,
    PRIMARY KEY (id),
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


CREATE TABLE studio_policy
(
    id                               BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id                        BIGINT      NOT NULL,
    reservation_close_minutes_before INT         NOT NULL,
    free_cancel_minutes_before       INT         NOT NULL,
    waiting_offer_response_minutes   INT         NOT NULL,
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
    category   VARCHAR(50) NOT NULL,
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
    is_system          BOOLEAN     NOT NULL DEFAULT FALSE,
    implies_instructor BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at         DATETIME(6) NOT NULL,
    updated_at         DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_studio_name (studio_id, name),
    CONSTRAINT fk_role_studio FOREIGN KEY (studio_id) REFERENCES studio (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE role_permission
(
    studio_role_id BIGINT NOT NULL,
    permission_id  BIGINT NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NULL,
    PRIMARY KEY (studio_role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (studio_role_id) REFERENCES studio_role (id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permission (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE studio_membership
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id       BIGINT      NOT NULL,
    member_id       BIGINT      NOT NULL,
    studio_role_id  BIGINT      NOT NULL,
    is_instructor   BOOLEAN     NOT NULL DEFAULT FALSE,
    is_customer     BOOLEAN     NOT NULL DEFAULT TRUE,
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
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    studio_id                BIGINT       NOT NULL,
    instructor_membership_id BIGINT       NOT NULL,
    name                     VARCHAR(100) NOT NULL,
    description              TEXT         NULL,
    capacity                 INT          NOT NULL,
    created_at               DATETIME(6)  NOT NULL,
    updated_at               DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_template_studio FOREIGN KEY (studio_id) REFERENCES studio (id),
    CONSTRAINT fk_template_instructor FOREIGN KEY (instructor_membership_id) REFERENCES studio_membership (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE class_session
(
    id                       BIGINT      NOT NULL AUTO_INCREMENT,
    studio_id                BIGINT      NOT NULL,
    class_template_id        BIGINT      NOT NULL,
    room_id                  BIGINT      NOT NULL,
    instructor_membership_id BIGINT      NOT NULL,
    description              TEXT        NULL,
    capacity                 INT         NOT NULL,
    start_at                 DATETIME(6) NOT NULL,
    end_at                   DATETIME(6) NOT NULL,
    status                   VARCHAR(20) NOT NULL,
    active_flag              TINYINT GENERATED ALWAYS AS (IF(status = 'CANCELED', NULL, 1)) STORED,
    created_at               DATETIME(6) NOT NULL,
    updated_at               DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_instructor_active (instructor_membership_id, start_at, active_flag),
    UNIQUE KEY uk_session_room_active (room_id, start_at, active_flag),
    CONSTRAINT fk_session_studio FOREIGN KEY (studio_id) REFERENCES studio (id),
    CONSTRAINT fk_session_template FOREIGN KEY (class_template_id) REFERENCES class_template (id),
    CONSTRAINT fk_session_room FOREIGN KEY (room_id) REFERENCES room (id),
    CONSTRAINT fk_session_instructor FOREIGN KEY (instructor_membership_id) REFERENCES studio_membership (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE pass_product
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    studio_id         BIGINT       NOT NULL,
    name              VARCHAR(100) NOT NULL,
    pass_product_type VARCHAR(20)  NOT NULL,
    total_count       INT          NULL,
    total_hold_days   INT          NOT NULL DEFAULT 0,
    valid_days        INT          NOT NULL,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pass_product_studio FOREIGN KEY (studio_id) REFERENCES studio (id)
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


CREATE TABLE reservation
(
    id                     BIGINT      NOT NULL AUTO_INCREMENT,
    membership_id          BIGINT      NOT NULL,
    class_session_id       BIGINT      NOT NULL,
    member_pass_product_id BIGINT      NULL,
    status                 VARCHAR(20) NOT NULL,
    active_flag            TINYINT GENERATED ALWAYS AS (IF(status = 'CANCELED', NULL, 1)) STORED,
    reserved_at            DATETIME(6) NOT NULL,
    canceled_at            DATETIME(6) NULL,
    created_at             DATETIME(6) NOT NULL,
    updated_at             DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservation_active (class_session_id, membership_id, active_flag),
    CONSTRAINT fk_reservation_membership FOREIGN KEY (membership_id) REFERENCES studio_membership (id),
    CONSTRAINT fk_reservation_session FOREIGN KEY (class_session_id) REFERENCES class_session (id),
    CONSTRAINT fk_reservation_member_pass_product FOREIGN KEY (member_pass_product_id) REFERENCES member_pass_product (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE waiting
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    membership_id    BIGINT      NOT NULL,
    class_session_id BIGINT      NOT NULL,
    sequence         INT         NOT NULL,
    status           VARCHAR(20) NOT NULL,
    active_flag      TINYINT GENERATED ALWAYS AS (IF(status IN ('CANCELED', 'EXPIRED'), NULL, 1)) STORED,
    offered_at       DATETIME(6) NULL,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_waiting_active (class_session_id, membership_id, active_flag),
    CONSTRAINT fk_waiting_membership FOREIGN KEY (membership_id) REFERENCES studio_membership (id),
    CONSTRAINT fk_waiting_session FOREIGN KEY (class_session_id) REFERENCES class_session (id)
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
