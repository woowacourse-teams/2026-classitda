package com.classitda.feature.auth.signup

import com.classitda.domain.model.auth.signup.GoogleIdToken
import com.classitda.domain.model.auth.signup.GoogleLoginResult
import com.classitda.domain.model.auth.signup.LoginTokens
import com.classitda.domain.model.auth.signup.PhoneVerificationChallenge
import com.classitda.domain.model.auth.signup.PhoneVerificationCode
import com.classitda.domain.model.auth.signup.PhoneVerificationId
import com.classitda.domain.model.auth.signup.SignupName
import com.classitda.domain.model.auth.signup.SignupPhoneNumber
import com.classitda.domain.model.auth.signup.SignupTerm
import com.classitda.domain.model.auth.signup.SignupToken
import com.classitda.domain.model.auth.signup.TermId
import com.classitda.domain.repository.auth.signup.SignupRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SignupViewModelTest {
    @BeforeTest
    fun setUpMainDispatcher() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `기존 회원의 Google 로그인은 회원가입 화면 없이 로그인 완료 상태가 된다`() =
        runBlocking {
            val viewModel = SignupViewModel(RegisteredMemberRepository())
            val loginCompletedEvent = async(start = CoroutineStart.UNDISPATCHED) {
                viewModel.events.first()
            }

            viewModel.loginWithGoogle("google-id-token")

            assertEquals(SignupEvent.LoginCompleted, loginCompletedEvent.await())
            assertEquals(SignupPage.Welcome, viewModel.uiState.value.page)
            Unit
        }

    @Test
    fun `Google 로그인 403 결과는 탈퇴 진행 이벤트를 발생시킨다`() =
        runBlocking {
            val viewModel = SignupViewModel(RegisteredMemberRepository(GoogleLoginResult.WithdrawalPending))
            val withdrawalPendingEvent = async(start = CoroutineStart.UNDISPATCHED) {
                viewModel.events.first()
            }

            viewModel.loginWithGoogle("google-id-token")

            assertEquals(SignupEvent.WithdrawalPending, withdrawalPendingEvent.await())
            Unit
        }
}

private class RegisteredMemberRepository(
    private val loginResult: GoogleLoginResult =
        GoogleLoginResult.Registered(
            LoginTokens(
                accessToken = "access-token",
                accessTokenExpiresInSeconds = 3600,
                refreshToken = "refresh-token",
                refreshTokenExpiresInSeconds = 2592000,
            ),
        ),
) : SignupRepository {
    override suspend fun loginWithGoogle(idToken: GoogleIdToken): GoogleLoginResult = loginResult

    override suspend fun getTerms(signupToken: SignupToken): List<SignupTerm> = error("사용하지 않는 동작입니다.")

    override suspend fun requestPhoneVerification(
        signupToken: SignupToken,
        phoneNumber: SignupPhoneNumber,
    ): PhoneVerificationChallenge = error("사용하지 않는 동작입니다.")

    override suspend fun confirmPhoneVerification(
        signupToken: SignupToken,
        verificationId: PhoneVerificationId,
        code: PhoneVerificationCode,
    ) = error("사용하지 않는 동작입니다.")

    override suspend fun completeSignup(
        signupToken: SignupToken,
        name: SignupName,
        agreedTermIds: List<TermId>,
    ): LoginTokens = error("사용하지 않는 동작입니다.")

    override suspend fun logout() = error("사용하지 않는 동작입니다.")
}
