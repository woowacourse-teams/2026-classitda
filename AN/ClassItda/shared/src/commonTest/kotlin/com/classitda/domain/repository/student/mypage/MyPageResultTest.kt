package com.classitda.domain.repository.student.mypage

import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.model.student.mypage.MyPageSummary
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MyPageResultTest {
    @Test
    fun `실패 결과는 반환 데이터 타입과 무관하게 사용할 수 있다`() {
        val failure = MyPageResult.Failure(MyPageFailureReason.NETWORK)

        val summaryResult: MyPageResult<MyPageSummary> = failure
        val profileResult: MyPageResult<MemberProfile> = failure

        assertEquals(failure, summaryResult)
        assertEquals(failure, profileResult)
    }

    @Test
    fun `실패 이유는 서버 표현이 아닌 확정된 도메인 의미만 제공한다`() {
        assertContentEquals(
            listOf(
                MyPageFailureReason.NETWORK,
                MyPageFailureReason.NOT_FOUND,
                MyPageFailureReason.CONFLICT,
                MyPageFailureReason.INVALID_REQUEST,
                MyPageFailureReason.VERIFICATION_EXPIRED,
                MyPageFailureReason.VERIFICATION_FAILED,
                MyPageFailureReason.UNKNOWN,
            ),
            MyPageFailureReason.entries,
        )
    }
}
