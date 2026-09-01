package com.pheeeew.fake

import com.pheeeew.domain.exception.ApiException
import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.sigh.SighBounds
import com.pheeeew.domain.model.sigh.SighPin
import com.pheeeew.domain.repository.SighRepository

class FakeSighRepository : SighRepository {
    private var getSighsResult: Result<List<SighPin>>? = null
    private var registerSighResult: Result<SighPin>? = null

    var getSighsCallCount: Int = 0
        private set
    var registerSighCallCount: Int = 0
        private set

    private val _receivedRequestIds = mutableListOf<String>()
    val receivedRequestIds: List<String>
        get() = _receivedRequestIds

    private val _receivedCoordinates = mutableListOf<Coordinate>()
    val receivedCoordinates: List<Coordinate>
        get() = _receivedCoordinates

    fun setGetSighsSuccess(sighs: List<SighPin>) {
        getSighsResult = Result.success(sighs)
    }

    fun setGetSighsFailure(exception: ApiException) {
        getSighsResult = Result.failure(exception)
    }

    fun setRegisterSighSuccess(sighPin: SighPin) {
        registerSighResult = Result.success(sighPin)
    }

    fun setRegisterSighFailure(exception: ApiException) {
        registerSighResult = Result.failure(exception)
    }

    override suspend fun getSighs(bounds: SighBounds): List<SighPin> {
        getSighsCallCount += 1

        return checkNotNull(getSighsResult) {
            "getSighs 결과를 먼저 생성해야 합니다."
        }.getOrThrow()
    }

    override suspend fun registerSigh(
        requestId: String,
        coordinate: Coordinate,
    ): SighPin {
        registerSighCallCount += 1
        _receivedRequestIds += requestId
        _receivedCoordinates += coordinate

        return checkNotNull(registerSighResult) {
            "registerSigh 결과를 먼저 설정해야 합니다."
        }.getOrThrow()
    }
}
