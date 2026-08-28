package com.classitda.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.classitda.data.local.instructor.mypage.InstructorMyPageCacheDao
import com.classitda.data.local.instructor.mypage.InstructorMyPageCacheEntity

@Database(
    entities = [InstructorMyPageCacheEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(ClassItdaDatabaseConstructor::class)
internal abstract class ClassItdaDatabase : RoomDatabase() {
    abstract fun instructorMyPageCacheDao(): InstructorMyPageCacheDao
}

@Suppress("KotlinNoActualForExpect")
internal expect object ClassItdaDatabaseConstructor : RoomDatabaseConstructor<ClassItdaDatabase> {
    override fun initialize(): ClassItdaDatabase
}
