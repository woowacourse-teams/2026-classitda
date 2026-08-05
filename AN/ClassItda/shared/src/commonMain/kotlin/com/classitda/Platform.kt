package com.classitda

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform