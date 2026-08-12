-- PermissionCode enum과 1:1로 대응하는 권한 카탈로그다.
-- enum에 상수를 추가하면 이 파일에도 함께 추가해야 한다.
INSERT INTO permission (code, created_at)
VALUES ('STUDIO_UPDATE', NOW(6)),
       ('POLICY_MANAGE', NOW(6)),
       ('ROOM_MANAGE', NOW(6)),
       ('ROLE_MANAGE', NOW(6)),
       ('MEMBER_READ', NOW(6)),
       ('MEMBER_INVITE', NOW(6)),
       ('MEMBER_MANAGE', NOW(6)),
       ('CLASS_TEMPLATE_MANAGE', NOW(6)),
       ('CLASS_SESSION_MANAGE_OWN', NOW(6)),
       ('CLASS_SESSION_MANAGE_ALL', NOW(6)),
       ('RESERVATION_READ', NOW(6)),
       ('RESERVATION_MANAGE', NOW(6)),
       ('PASS_PRODUCT_MANAGE', NOW(6)),
       ('PASS_MEMBER_MANAGE', NOW(6)),
       ('NOTICE_MANAGE', NOW(6));
