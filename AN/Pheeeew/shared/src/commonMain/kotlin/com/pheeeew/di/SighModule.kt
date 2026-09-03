package com.pheeeew.di

import com.pheeeew.core.network.ApiConfig
import com.pheeeew.core.network.createPlatformHttpClient
import com.pheeeew.data.remote.sigh.DefaultSighApi
import com.pheeeew.data.repository.DefaultSighRepository
import com.pheeeew.domain.repository.SighRepository

object SighModule {
    fun create(config: ApiConfig): SighRepository =
        DefaultSighRepository(DefaultSighApi(createPlatformHttpClient(config)))
}
