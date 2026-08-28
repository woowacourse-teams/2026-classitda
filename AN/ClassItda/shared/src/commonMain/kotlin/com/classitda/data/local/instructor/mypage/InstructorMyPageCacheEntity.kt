package com.classitda.data.local.instructor.mypage

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "instructor_my_page_cache")
internal data class InstructorMyPageCacheEntity(
    @PrimaryKey
    val slot: Int = ACTIVE_PROFILE_SLOT,
    val name: String,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
)

internal const val ACTIVE_PROFILE_SLOT = 0
