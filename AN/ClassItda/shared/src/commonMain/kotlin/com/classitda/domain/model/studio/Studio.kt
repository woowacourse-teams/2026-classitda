package com.classitda.domain.model.studio

import kotlinx.datetime.LocalTime

data class Studio(
    val id: StudioId,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val openTime: LocalTime?,
    val closeTime: LocalTime?,
    val imageUrl: String?,
    val description: String?,
)
