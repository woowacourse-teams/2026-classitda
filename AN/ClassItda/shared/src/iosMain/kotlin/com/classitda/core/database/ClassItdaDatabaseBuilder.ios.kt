package com.classitda.core.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun createPlatformDatabaseModule(): AppDatabaseModule {
    val cachesUrl =
        requireNotNull(
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSCachesDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            ),
        )
    val path = requireNotNull(cachesUrl.URLByAppendingPathComponent(DATABASE_NAME)?.path)
    val builder =
        Room.databaseBuilder<ClassItdaDatabase>(
            name = path,
            factory = ClassItdaDatabaseConstructor::initialize,
        )
    return AppDatabaseModule(createDatabaseModule(builder))
}
