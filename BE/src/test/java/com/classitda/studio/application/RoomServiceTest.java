package com.classitda.studio.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.member.domain.Member;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.RoomFixture;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.presentation.dto.RoomResponse;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.support.MySqlTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Import(MySqlTestContainerConfiguration.class)
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private StudioService studioService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 룸을_등록하면_룸_정보를_반환한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when
        RoomResponse response = roomService.save(owner.getId(), studioId, RoomFixture.기본_룸_생성_요청());

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("A룸");
    }

    @Test
    void 같은_시설에_같은_이름의_룸을_등록할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        roomService.save(owner.getId(), studioId, RoomFixture.기본_룸_생성_요청());

        // when / then
        assertThatThrownBy(() -> roomService.save(owner.getId(), studioId, RoomFixture.기본_룸_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.ROOM_NAME_DUPLICATED.getMessage());
    }

    @Test
    void 소속이_아니면_룸을_등록할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Member other = StudioFixture.아이디가_다른_소유자("other");
        entityManager.persist(other);
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> roomService.save(other.getId(), studioId, RoomFixture.기본_룸_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_MEMBERSHIP.getMessage());
    }

    @Test
    void 없는_시설에는_룸을_등록할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();

        // when / then
        assertThatThrownBy(() -> roomService.save(owner.getId(), 999L, RoomFixture.기본_룸_생성_요청()))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    void 룸_목록을_커서로_나누어_조회한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        roomService.save(owner.getId(), studioId, RoomFixture.이름이_다른_룸_생성_요청("A룸"));
        roomService.save(owner.getId(), studioId, RoomFixture.이름이_다른_룸_생성_요청("B룸"));
        roomService.save(owner.getId(), studioId, RoomFixture.이름이_다른_룸_생성_요청("C룸"));
        entityManager.flush();

        // when
        CursorResponse<RoomResponse> firstPage = roomService.findWithCursor(studioId, null, 2);

        // then
        assertThat(firstPage.items()).extracting(RoomResponse::name).containsExactly("A룸", "B룸");
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.nextCursor()).isNotNull();

        // when
        CursorResponse<RoomResponse> secondPage = roomService.findWithCursor(studioId, firstPage.nextCursor(), 2);

        // then
        assertThat(secondPage.items()).extracting(RoomResponse::name).containsExactly("C룸");
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.nextCursor()).isNull();
    }

    @Test
    void 룸이_없으면_빈_목록을_반환한다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when
        CursorResponse<RoomResponse> response = roomService.findWithCursor(studioId, null, 20);

        // then
        assertThat(response.items()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void size가_양수가_아니면_조회할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> roomService.findWithCursor(studioId, null, 0))
                .isInstanceOf(ClassitdaException.class)
                .hasMessage(CommonErrorCode.INVALID_INPUT.getMessage());
    }

    @Test
    void size가_상한을_넘으면_조회할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> roomService.findWithCursor(studioId, null, 101))
                .isInstanceOf(ClassitdaException.class)
                .hasMessage(CommonErrorCode.INVALID_INPUT.getMessage());
    }

    @Test
    void 커서가_숫자가_아니면_조회할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> roomService.findWithCursor(studioId, "abc", 20))
                .isInstanceOf(ClassitdaException.class)
                .hasMessage(CommonErrorCode.INVALID_INPUT.getMessage());
    }

    @Test
    void 다른_시설의_룸은_조회되지_않는다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        Long otherStudioId = 시설을_만든다(owner, "다른 스튜디오");
        roomService.save(owner.getId(), studioId, RoomFixture.기본_룸_생성_요청());
        entityManager.flush();

        // when
        CursorResponse<RoomResponse> response = roomService.findWithCursor(otherStudioId, null, 20);

        // then
        assertThat(response.items()).isEmpty();
    }

    @Test
    void 대표_강사는_룸_정보를_수정할_수_있다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        RoomResponse created = roomService.save(owner.getId(), studioId, RoomFixture.기본_룸_생성_요청());
        entityManager.flush();

        // when
        RoomResponse response = roomService.update(
                owner.getId(), studioId, created.id(), RoomFixture.이름만_바꾸는_수정_요청("B룸"));

        // then
        assertThat(response.name()).isEqualTo("B룸");
    }

    @Test
    void 소속이_아니면_룸을_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Member other = StudioFixture.아이디가_다른_소유자("other");
        entityManager.persist(other);
        Long studioId = 시설을_만든다(owner);
        RoomResponse created = roomService.save(owner.getId(), studioId, RoomFixture.기본_룸_생성_요청());
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> roomService.update(
                other.getId(), studioId, created.id(), RoomFixture.이름만_바꾸는_수정_요청("B룸")))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.NOT_MEMBERSHIP.getMessage());
    }

    @Test
    void 다른_시설의_룸은_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        Long otherStudioId = 시설을_만든다(owner, "다른 스튜디오");
        RoomResponse created = roomService.save(owner.getId(), studioId, RoomFixture.기본_룸_생성_요청());
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> roomService.update(
                owner.getId(), otherStudioId, created.id(), RoomFixture.이름만_바꾸는_수정_요청("B룸")))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.ROOM_NOT_FOUND.getMessage());
    }

    @Test
    void 없는_룸은_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);

        // when / then
        assertThatThrownBy(() -> roomService.update(
                owner.getId(), studioId, 999L, RoomFixture.이름만_바꾸는_수정_요청("B룸")))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.ROOM_NOT_FOUND.getMessage());
    }

    @Test
    void 다른_룸과_이름이_겹치도록_수정할_수_없다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        roomService.save(owner.getId(), studioId, RoomFixture.이름이_다른_룸_생성_요청("A룸"));
        RoomResponse target = roomService.save(owner.getId(), studioId, RoomFixture.이름이_다른_룸_생성_요청("B룸"));
        entityManager.flush();

        // when / then
        assertThatThrownBy(() -> roomService.update(
                owner.getId(), studioId, target.id(), RoomFixture.이름만_바꾸는_수정_요청("A룸")))
                .isInstanceOf(StudioException.class)
                .hasMessage(StudioErrorCode.ROOM_NAME_DUPLICATED.getMessage());
    }

    @Test
    void 이름을_그대로_두고_수정해도_중복으로_막히지_않는다() {
        // given
        Member owner = 소유자를_저장한다();
        Long studioId = 시설을_만든다(owner);
        RoomResponse created = roomService.save(owner.getId(), studioId, RoomFixture.기본_룸_생성_요청());
        entityManager.flush();

        // when
        RoomResponse response = roomService.update(
                owner.getId(), studioId, created.id(), RoomFixture.이름만_바꾸는_수정_요청("A룸"));

        // then
        assertThat(response.name()).isEqualTo("A룸");
    }

    private Member 소유자를_저장한다() {
        Member owner = StudioFixture.기본_소유자();
        entityManager.persist(owner);
        entityManager.flush();
        return owner;
    }

    private Long 시설을_만든다(Member owner) {
        return studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청()).id();
    }

    private Long 시설을_만든다(Member owner, String name) {
        StudioResponse response = studioService.save(owner.getId(), StudioFixture.기본_시설_생성_요청());
        entityManager.flush();
        return studioService.update(owner.getId(), response.id(), StudioFixture.이름만_바꾸는_수정_요청(name)).id();
    }
}
