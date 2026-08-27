package com.classitda.core.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.classitda.core.auth.SessionCacheCleaner
import com.classitda.data.local.instructor.mypage.InstructorMyPageCache
import com.classitda.data.local.instructor.mypage.RoomInstructorMyPageCache
import org.koin.core.module.Module
import org.koin.dsl.module

internal const val DATABASE_NAME = "classitda.db"

class AppDatabaseModule internal constructor(
    internal val koinModule: Module,
)

internal fun createDatabaseModule(builder: RoomDatabase.Builder<ClassItdaDatabase>): Module =
    module {
        single<ClassItdaDatabase> {
            builder
                .setDriver(BundledSQLiteDriver())
                .build()
        }
        single { get<ClassItdaDatabase>().instructorMyPageCacheDao() }
        single<RoomInstructorMyPageCache> { RoomInstructorMyPageCache(get()) }
        single<InstructorMyPageCache> { get<RoomInstructorMyPageCache>() }
        single<SessionCacheCleaner> { get<RoomInstructorMyPageCache>() }
    }

fun createInMemoryDatabaseModule(): AppDatabaseModule =
    AppDatabaseModule(
        createDatabaseModule(
            Room.inMemoryDatabaseBuilder<ClassItdaDatabase>(
                factory = ClassItdaDatabaseConstructor::initialize,
            ),
        ),
    )
