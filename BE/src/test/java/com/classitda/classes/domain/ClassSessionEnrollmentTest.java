package com.classitda.classes.domain;

import static com.classitda.classes.fixture.ClassSessionFixture.기본_수업_회차;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.studio.domain.StudioMembership;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ClassSessionEnrollmentTest {

    private static final LocalDateTime WAITED_AT =
            LocalDateTime.of(2026, 8, 20, 9, 0);
    private static final LocalDateTime OFFERED_AT = WAITED_AT.plusHours(1);
    private static final LocalDateTime OFFER_EXPIRES_AT = OFFERED_AT.plusMinutes(10);
    private static final LocalDateTime ACCEPTED_AT = OFFERED_AT.plusMinutes(5);

    private final ClassSession classSession = 기본_수업_회차();
    private final StudioMembership membership = classSession.getInstructorMembership();
    private final MemberPassProduct memberPassProduct = MemberPassProduct.builder().build();

    @Test
    void 대기_신청은_수강권을_연결하지_않는다() {
        ClassSessionEnrollment enrollment = 대기_신청();

        assertThat(enrollment.getMembership()).isSameAs(membership);
        assertThat(enrollment.getClassSession()).isSameAs(classSession);
        assertThat(enrollment.getMemberPassProduct()).isNull();
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.WAITING);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(WAITED_AT);
        assertThat(enrollment.getAttendance().getResult())
                .isEqualTo(AttendanceResult.NOT_RECORDED);
    }

    @Test
    void 즉시_예약은_수강권을_필수로_연결한다() {
        ClassSessionEnrollment enrollment = 예약_신청();

        assertThat(enrollment.getMemberPassProduct()).isSameAs(memberPassProduct);
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.RESERVED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(WAITED_AT);
    }

    @Test
    void 수강권_없이_즉시_예약할_수_없다() {
        assertThatThrownBy(() -> ClassSessionEnrollment.reserved(
                membership,
                classSession,
                null,
                WAITED_AT
        )).isInstanceOfSatisfying(ClassException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ClassErrorCode.ENROLLMENT_MEMBER_PASS_PRODUCT_REQUIRED));
    }

    @Test
    void 대기_신청의_필수_관계와_발생_시각을_검증한다() {
        assertThatThrownBy(() -> ClassSessionEnrollment.waiting(
                null,
                classSession,
                WAITED_AT
        )).isInstanceOfSatisfying(ClassException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ClassErrorCode.ENROLLMENT_MEMBERSHIP_REQUIRED));
        assertThatThrownBy(() -> ClassSessionEnrollment.waiting(
                membership,
                null,
                WAITED_AT
        )).isInstanceOfSatisfying(ClassException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ClassErrorCode.ENROLLMENT_CLASS_SESSION_REQUIRED));
        assertThatThrownBy(() -> ClassSessionEnrollment.waiting(
                membership,
                classSession,
                null
        )).isInstanceOfSatisfying(ClassException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ClassErrorCode.ENROLLMENT_OCCURRED_AT_REQUIRED));
    }

    @Test
    void 대기_중인_신청에_제안하면_기한과_상태_변경_시각을_기록한다() {
        ClassSessionEnrollment enrollment = 대기_신청();

        enrollment.offer(OFFERED_AT, OFFER_EXPIRES_AT);

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.OFFERED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(OFFERED_AT);
        assertThat(enrollment.getOfferExpiresAt()).isEqualTo(OFFER_EXPIRES_AT);
        assertThat(enrollment.getMemberPassProduct()).isNull();
    }

    @Test
    void 제안_기한은_제안_시각보다_뒤여야_하며_실패하면_상태를_유지한다() {
        ClassSessionEnrollment enrollment = 대기_신청();

        assertThatThrownBy(() -> enrollment.offer(OFFERED_AT, OFFERED_AT))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_ENROLLMENT_OFFER_DEADLINE));
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.WAITING);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(WAITED_AT);
        assertThat(enrollment.getOfferExpiresAt()).isNull();
    }

    @Test
    void 제안을_수락할_때_수강권을_연결하고_제안_기한을_비운다() {
        ClassSessionEnrollment enrollment = 제안된_신청();

        enrollment.acceptOffer(memberPassProduct, ACCEPTED_AT);

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.RESERVED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(ACCEPTED_AT);
        assertThat(enrollment.getMemberPassProduct()).isSameAs(memberPassProduct);
        assertThat(enrollment.getOfferExpiresAt()).isNull();
    }

    @Test
    void 제안_만료_시각부터는_수락할_수_없으며_수강권도_연결하지_않는다() {
        ClassSessionEnrollment enrollment = 제안된_신청();

        assertThatThrownBy(() -> enrollment.acceptOffer(
                memberPassProduct,
                OFFER_EXPIRES_AT
        )).isInstanceOfSatisfying(ClassException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ClassErrorCode.ENROLLMENT_OFFER_EXPIRED));
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.OFFERED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(OFFERED_AT);
        assertThat(enrollment.getMemberPassProduct()).isNull();
        assertThat(enrollment.getOfferExpiresAt()).isEqualTo(OFFER_EXPIRES_AT);
    }

    @Test
    void 제안된_대기를_취소하면_제안_기한을_비운다() {
        ClassSessionEnrollment enrollment = 제안된_신청();
        LocalDateTime canceledAt = OFFERED_AT.plusMinutes(1);

        enrollment.cancelWaiting(canceledAt);

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.CANCELED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(canceledAt);
        assertThat(enrollment.getOfferExpiresAt()).isNull();
        assertThat(enrollment.getMemberPassProduct()).isNull();
    }

    @Test
    void 제안된_대기를_만료하면_제안_기한을_비운다() {
        ClassSessionEnrollment enrollment = 제안된_신청();

        enrollment.expire(OFFER_EXPIRES_AT);

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.EXPIRED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(OFFER_EXPIRES_AT);
        assertThat(enrollment.getOfferExpiresAt()).isNull();
        assertThat(enrollment.getMemberPassProduct()).isNull();
    }

    @Test
    void 예약을_취소해도_사용한_수강권_연결을_이력으로_유지한다() {
        ClassSessionEnrollment enrollment = 예약_신청();
        LocalDateTime canceledAt = WAITED_AT.plusHours(1);

        enrollment.cancelReservation(canceledAt);

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.CANCELED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(canceledAt);
        assertThat(enrollment.getMemberPassProduct()).isSameAs(memberPassProduct);
    }

    @Test
    void 출결을_기록한_예약은_취소할_수_없다() {
        ClassSessionEnrollment enrollment = 예약_신청();
        LocalDateTime attendedAt = WAITED_AT.plusDays(1);
        enrollment.markAttended(attendedAt);

        assertThatThrownBy(() -> enrollment.cancelReservation(attendedAt.plusMinutes(1)))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_ENROLLMENT_TRANSITION));
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.RESERVED);
        assertThat(enrollment.getAttendance().getResult())
                .isEqualTo(AttendanceResult.ATTENDED);
    }

    @Test
    void 예약_확정_상태에서만_출결을_기록할_수_있다() {
        ClassSessionEnrollment enrollment = 대기_신청();

        assertThatThrownBy(() -> enrollment.markAttended(WAITED_AT.plusDays(1)))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.INVALID_ENROLLMENT_TRANSITION));
        assertThat(enrollment.getAttendance().getResult())
                .isEqualTo(AttendanceResult.NOT_RECORDED);
    }

    @Test
    void 예약의_출석을_결석으로_정정해도_신청_상태는_예약으로_유지한다() {
        ClassSessionEnrollment enrollment = 예약_신청();
        LocalDateTime attendedAt = WAITED_AT.plusDays(1);
        LocalDateTime absentAt = attendedAt.plusMinutes(5);

        enrollment.markAttended(attendedAt);
        enrollment.markAbsent(absentAt);

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.RESERVED);
        assertThat(enrollment.getAttendance().getResult()).isEqualTo(AttendanceResult.ABSENT);
        assertThat(enrollment.getAttendance().getRecordedAt()).isEqualTo(absentAt);
    }

    @Test
    void 종료된_신청은_다시_활성_상태로_전이할_수_없다() {
        ClassSessionEnrollment enrollment = 대기_신청();
        enrollment.expire(OFFER_EXPIRES_AT);

        assertThatThrownBy(() -> enrollment.offer(
                OFFER_EXPIRES_AT.plusMinutes(1),
                OFFER_EXPIRES_AT.plusMinutes(2)
        )).isInstanceOfSatisfying(ClassException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ClassErrorCode.INVALID_ENROLLMENT_TRANSITION));
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.EXPIRED);
        assertThat(enrollment.getEnrollmentStatusChangedAt()).isEqualTo(OFFER_EXPIRES_AT);
    }

    private ClassSessionEnrollment 대기_신청() {
        return ClassSessionEnrollment.waiting(membership, classSession, WAITED_AT);
    }

    private ClassSessionEnrollment 제안된_신청() {
        ClassSessionEnrollment enrollment = 대기_신청();
        enrollment.offer(OFFERED_AT, OFFER_EXPIRES_AT);
        return enrollment;
    }

    private ClassSessionEnrollment 예약_신청() {
        return ClassSessionEnrollment.reserved(
                membership,
                classSession,
                memberPassProduct,
                WAITED_AT
        );
    }
}
