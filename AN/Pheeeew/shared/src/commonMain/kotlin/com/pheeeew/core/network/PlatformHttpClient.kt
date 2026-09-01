package com.pheeeew.core.network

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(config: ApiConfig): HttpClient
