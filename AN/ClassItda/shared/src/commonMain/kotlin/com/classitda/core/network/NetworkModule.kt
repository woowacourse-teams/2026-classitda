package com.classitda.core.network

import com.classitda.core.auth.AuthTokenStorage
import com.classitda.core.auth.SessionCacheCleaner
import io.ktor.client.HttpClient
import org.koin.dsl.module

/**
 * 네트워크 객체를 Koin에 등록한다.
 *
 * base URL은 환경마다 다를 수 있으므로 여기서 값을 결정하지 않고 호출자가
 * NetworkConfig로 주입한다.
 */
fun networkModule(
    config: NetworkConfig,
    tokenStorage: AuthTokenStorage,
) = module {
    single { config }
    single<HttpClient> {
        createConfiguredHttpClient(
            config = get(),
            tokenStorage = tokenStorage,
            sessionCacheCleaner = getOrNull<SessionCacheCleaner>(),
        )
    }
}
