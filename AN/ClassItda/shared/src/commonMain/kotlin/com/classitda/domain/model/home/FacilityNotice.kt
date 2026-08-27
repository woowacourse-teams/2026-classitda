package com.classitda.domain.model.home

import kotlinx.datetime.LocalDate

data class FacilityNotice(
    val id: String,
    val title: String,
    val description: String,
    val postedDate: LocalDate,
)
