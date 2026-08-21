package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.domain.ClassGuest;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.Reservation;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.application.ClassTypeService;
import com.classitda.classes.domain.repository.ClassGuestRepository;
import com.classitda.classes.domain.repository.ReservationRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ReservationFixture;
import com.classitda.classes.presentation.dto.ReservationResponse;
import com.classitda.member.domain.Member;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.application.StudioService;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({ReservationService.class, StudioService.class, StudioPermissionService.class,
        ClassTypeService.class, ReservationServiceTest.FixedClockConfiguration.class})
@MySqlRepositoryTest
class ReservationServiceTest {

    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    private final ReservationService reservationService;
    private final StudioService studioService;
    private final ReservationRepository reservationRepository;
    private final ClassGuestRepository classGuestRepository;
    private final StudioRepository studioRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final EntityManager entityManager;

    @Autowired
    ReservationServiceTest(
            ReservationService reservationService,
            StudioService studioService,
            ReservationRepository reservationRepository,
            ClassGuestRepository classGuestRepository,
            StudioRepository studioRepository,
            StudioRoleRepository studioRoleRepository,
            EntityManager entityManager
    ) {
        this.reservationService = reservationService;
        this.studioService = studioService;
        this.reservationRepository = reservationRepository;
        this.classGuestRepository = classGuestRepository;
        this.studioRepository = studioRepository;
        this.studioRoleRepository = studioRoleRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 담당_강사가_회원을_수업에_등록한다() {
        // given
        Studio studio = 시설을_저장한다("register-member");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership student = 소속을_저장한다(studio, "회원", SystemRole.STUDENT);
        ClassSession session = 수업을_저장한다(studio, instructor);

        // when
        ReservationResponse response = reservationService.save(
                instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.회원_등록_요청(student.getId()));

        // then
        Reservation saved = reservationRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(saved.getMembership().getId()).isEqualTo(student.getId());
        assertThat(saved.getClassGuest()).isNull();
        assertThat(response.attendeeName()).isEqualTo("회원");
    }

    @Test
    void 담당_강사가_비회원을_이름만으로_등록한다() {
        // given
        Studio studio = 시설을_저장한다("register-guest");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        ClassSession session = 수업을_저장한다(studio, instructor);

        // when
        ReservationResponse response = reservationService.save(
                instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("현장 손님"));

        // then
        Reservation saved = reservationRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getMembership()).isNull();
        assertThat(saved.isGuestReservation()).isTrue();
        assertThat(response.attendeeName()).isEqualTo("현장 손님");
        assertThat(classGuestRepository.count()).isEqualTo(1);
    }

    @Test
    void 비회원을_등록할_때마다_비회원_행이_새로_생긴다() {
        // given
        Studio studio = 시설을_저장한다("guest-per-registration");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        ClassSession first = 수업을_저장한다(studio, instructor);
        ClassSession second = 시작_시각이_다른_수업을_저장한다(
                studio, instructor, ReservationFixture.기준_시각.plusDays(2));

        // when
        reservationService.save(instructor.getMember().getId(), studio.getId(), first.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("김손님"));
        reservationService.save(instructor.getMember().getId(), studio.getId(), second.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("김손님"));

        // then
        assertThat(classGuestRepository.count()).isEqualTo(2);
    }

    @Test
    void 담당_강사가_아니면_등록할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("not-in-charge");
        StudioMembership owner = 소속을_저장한다(studio, "대표", SystemRole.OWNER);
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership student = 소속을_저장한다(studio, "회원", SystemRole.STUDENT);
        ClassSession session = 수업을_저장한다(studio, instructor);

        // when / then
        assertThatThrownBy(() -> reservationService.save(
                owner.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.회원_등록_요청(student.getId())))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_SESSION_NOT_MANAGEABLE));
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    void 같은_수업에_같은_회원을_두_번_등록할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("duplicated");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership student = 소속을_저장한다(studio, "회원", SystemRole.STUDENT);
        ClassSession session = 수업을_저장한다(studio, instructor);
        reservationService.save(instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.회원_등록_요청(student.getId()));

        // when / then
        assertThatThrownBy(() -> reservationService.save(
                instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.회원_등록_요청(student.getId())))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_DUPLICATED));
    }

    @Test
    void 같은_시간에_겹치는_다른_수업에는_등록할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("overlapped");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership student = 소속을_저장한다(studio, "회원", SystemRole.STUDENT);
        ClassSession first = 수업을_저장한다(studio, instructor);
        ClassSession overlapping = 시작_시각이_다른_수업을_저장한다(
                studio, instructor, first.getStartAt().plusMinutes(30));
        reservationService.save(instructor.getMember().getId(), studio.getId(), first.getId(),
                ReservationFixture.회원_등록_요청(student.getId()));

        // when / then
        assertThatThrownBy(() -> reservationService.save(
                instructor.getMember().getId(), studio.getId(), overlapping.getId(),
                ReservationFixture.회원_등록_요청(student.getId())))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_TIME_OVERLAPPED));
    }

    @Test
    void 시간이_겹치지_않으면_다른_수업에_등록할_수_있다() {
        // given
        Studio studio = 시설을_저장한다("not-overlapped");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership student = 소속을_저장한다(studio, "회원", SystemRole.STUDENT);
        ClassSession first = 수업을_저장한다(studio, instructor);
        ClassSession next = 시작_시각이_다른_수업을_저장한다(
                studio, instructor, first.getEndAt());
        reservationService.save(instructor.getMember().getId(), studio.getId(), first.getId(),
                ReservationFixture.회원_등록_요청(student.getId()));

        // when
        reservationService.save(instructor.getMember().getId(), studio.getId(), next.getId(),
                ReservationFixture.회원_등록_요청(student.getId()));

        // then
        assertThat(reservationRepository.count()).isEqualTo(2);
    }

    @Test
    void 비회원은_시간이_겹쳐도_등록할_수_있다() {
        // given
        Studio studio = 시설을_저장한다("guest-overlap-allowed");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        ClassSession first = 수업을_저장한다(studio, instructor);
        ClassSession overlapping = 시작_시각이_다른_수업을_저장한다(
                studio, instructor, first.getStartAt().plusMinutes(30));
        reservationService.save(instructor.getMember().getId(), studio.getId(), first.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("김손님"));

        // when
        reservationService.save(instructor.getMember().getId(), studio.getId(), overlapping.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("김손님"));

        // then
        assertThat(reservationRepository.count()).isEqualTo(2);
    }

    @Test
    void 정원이_가득_차면_등록할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("capacity");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        ClassSession session = 정원이_하나인_수업을_저장한다(studio, instructor);
        reservationService.save(instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("먼저 온 손님"));

        // when / then
        assertThatThrownBy(() -> reservationService.save(
                instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("나중에 온 손님")))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_CAPACITY_EXCEEDED));
    }

    @Test
    void 취소한_예약은_정원에서_빠진다() {
        // given
        Studio studio = 시설을_저장한다("capacity-after-cancel");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        ClassSession session = 정원이_하나인_수업을_저장한다(studio, instructor);
        ReservationResponse first = reservationService.save(
                instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("먼저 온 손님"));
        reservationService.cancel(
                instructor.getMember().getId(), studio.getId(), session.getId(), first.id());
        entityManager.flush();
        entityManager.clear();

        // when
        reservationService.save(instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("나중에 온 손님"));

        // then
        assertThat(reservationRepository.countByClassSessionIdAndStatusNot(
                session.getId(), ReservationStatus.CANCELED)).isEqualTo(1);
    }

    @Test
    void 강사는_수업_참여자로_등록할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("instructor-not-allowed");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership other = 소속을_저장한다(studio, "다른 강사", SystemRole.INSTRUCTOR);
        ClassSession session = 수업을_저장한다(studio, instructor);

        // when / then
        assertThatThrownBy(() -> reservationService.save(
                instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.회원_등록_요청(other.getId())))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_INSTRUCTOR_NOT_ALLOWED));
    }

    @Test
    void 이미_끝난_수업에는_등록할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("ended-session");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        ClassSession ended = 시작_시각이_다른_수업을_저장한다(
                studio, instructor, ReservationFixture.기준_시각.minusDays(1));

        // when / then
        assertThatThrownBy(() -> reservationService.save(
                instructor.getMember().getId(), studio.getId(), ended.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("늦은 손님")))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_SESSION_CLOSED));
    }

    @Test
    void 다른_시설의_수업은_등록할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("own-studio");
        Studio otherStudio = 시설을_저장한다("other-studio");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership otherInstructor = 소속을_저장한다(otherStudio, "다른 강사", SystemRole.INSTRUCTOR);
        ClassSession otherSession = 수업을_저장한다(otherStudio, otherInstructor);

        // when / then
        assertThatThrownBy(() -> reservationService.save(
                instructor.getMember().getId(), studio.getId(), otherSession.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("손님")))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.CLASS_SESSION_NOT_FOUND));
    }

    @Test
    void 담당_강사가_예약을_취소하면_상태만_바뀐다() {
        // given
        Studio studio = 시설을_저장한다("cancel");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership student = 소속을_저장한다(studio, "회원", SystemRole.STUDENT);
        ClassSession session = 수업을_저장한다(studio, instructor);
        ReservationResponse saved = reservationService.save(
                instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.회원_등록_요청(student.getId()));

        // when
        reservationService.cancel(
                instructor.getMember().getId(), studio.getId(), session.getId(), saved.id());
        entityManager.flush();
        entityManager.clear();

        // then
        Reservation canceled = reservationRepository.findById(saved.id()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(canceled.getCanceledAt()).isEqualTo(ReservationFixture.기준_시각);
        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @Test
    void 취소한_회원을_같은_수업에_다시_등록할_수_있다() {
        // given
        Studio studio = 시설을_저장한다("re-register");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        StudioMembership student = 소속을_저장한다(studio, "회원", SystemRole.STUDENT);
        ClassSession session = 수업을_저장한다(studio, instructor);
        ReservationResponse saved = reservationService.save(
                instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.회원_등록_요청(student.getId()));
        reservationService.cancel(
                instructor.getMember().getId(), studio.getId(), session.getId(), saved.id());
        entityManager.flush();
        entityManager.clear();

        // when
        reservationService.save(instructor.getMember().getId(), studio.getId(), session.getId(),
                ReservationFixture.회원_등록_요청(student.getId()));

        // then
        assertThat(reservationRepository.count()).isEqualTo(2);
    }

    @Test
    void 없는_예약은_취소할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("cancel-not-found");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        ClassSession session = 수업을_저장한다(studio, instructor);

        // when / then
        assertThatThrownBy(() -> reservationService.cancel(
                instructor.getMember().getId(), studio.getId(), session.getId(), 9_999L))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.RESERVATION_NOT_FOUND));
    }

    @Test
    void 소속이_아니면_예약을_관리할_수_없다() {
        // given
        Studio studio = 시설을_저장한다("outsider");
        StudioMembership instructor = 소속을_저장한다(studio, "강사", SystemRole.INSTRUCTOR);
        ClassSession session = 수업을_저장한다(studio, instructor);
        Member outsider = 회원을_저장한다("outsider-member");

        // when / then
        assertThatThrownBy(() -> reservationService.save(
                outsider.getId(), studio.getId(), session.getId(),
                ReservationFixture.이름이_다른_비회원_등록_요청("손님")))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(StudioErrorCode.NOT_MEMBERSHIP));
    }

    private Studio 시설을_저장한다(String providerId) {
        Member owner = 회원을_저장한다(providerId + "-owner");
        Long studioId = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
        entityManager.flush();
        return studioRepository.findById(studioId).orElseThrow();
    }

    private Member 회원을_저장한다(String providerId) {
        Member member = StudioFixture.아이디가_다른_소유자(providerId);
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private StudioMembership 소속을_저장한다(Studio studio, String name, SystemRole systemRole) {
        Member member = 회원을_저장한다(studio.getId() + "-" + name);
        StudioRole role = 역할을_찾는다(studio, systemRole);
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .name(name)
                .studioRole(role)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(ReservationFixture.기준_시각)
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
    }

    private StudioRole 역할을_찾는다(Studio studio, SystemRole systemRole) {
        return studioRoleRepository.findAllByStudioId(studio.getId()).stream()
                .filter(role -> role.getSystemRole() == systemRole)
                .findFirst()
                .orElseThrow();
    }

    private ClassSession 수업을_저장한다(Studio studio, StudioMembership instructor) {
        return 시작_시각이_다른_수업을_저장한다(studio, instructor, ReservationFixture.기준_시각.plusDays(1));
    }

    private ClassSession 시작_시각이_다른_수업을_저장한다(
            Studio studio,
            StudioMembership instructor,
            LocalDateTime startAt
    ) {
        ClassSession session = ReservationFixture.시작_시각이_다른_수업(studio.getId(), instructor, startAt);
        entityManager.persist(session);
        entityManager.flush();
        return session;
    }

    private ClassSession 정원이_하나인_수업을_저장한다(Studio studio, StudioMembership instructor) {
        ClassSession session = ReservationFixture.정원이_다른_수업(
                studio.getId(), instructor, ReservationFixture.기준_시각.plusDays(1), 1);
        entityManager.persist(session);
        entityManager.flush();
        return session;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfiguration {

        @Bean
        Clock clock() {
            return Clock.fixed(ReservationFixture.기준_시각.atZone(KOREA).toInstant(), KOREA);
        }
    }
}
