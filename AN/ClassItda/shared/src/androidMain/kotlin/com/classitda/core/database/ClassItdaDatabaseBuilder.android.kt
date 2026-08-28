package com.classitda.core.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

fun createPlatformDatabaseModule(context: Context): AppDatabaseModule =
    AppDatabaseModule(createDatabaseModule(createClassItdaDatabaseBuilder(context)))

private fun createClassItdaDatabaseBuilder(context: Context): RoomDatabase.Builder<ClassItdaDatabase> {
    val appContext = context.applicationContext
    val path = appContext.noBackupFilesDir.resolve(DATABASE_NAME).absolutePath
    return Room.databaseBuilder(
        context = appContext,
        name = path,
        factory = ClassItdaDatabaseConstructor::initialize,
    )
}
