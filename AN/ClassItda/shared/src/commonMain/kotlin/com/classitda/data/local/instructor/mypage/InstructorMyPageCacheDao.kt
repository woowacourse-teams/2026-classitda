package com.classitda.data.local.instructor.mypage

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
internal interface InstructorMyPageCacheDao {
    @Query("SELECT * FROM instructor_my_page_cache WHERE slot = 0 LIMIT 1")
    suspend fun get(): InstructorMyPageCacheEntity?

    @Upsert
    suspend fun upsert(entity: InstructorMyPageCacheEntity)

    @Query("DELETE FROM instructor_my_page_cache")
    suspend fun clear()
}
