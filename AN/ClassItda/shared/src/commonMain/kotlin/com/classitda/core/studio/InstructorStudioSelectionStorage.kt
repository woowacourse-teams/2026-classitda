package com.classitda.core.studio

import com.russhwolf.settings.Settings

interface InstructorStudioSelectionStorage {
    fun read(): String?

    fun save(studioId: String)

    fun clear()
}

class SettingsInstructorStudioSelectionStorage(
    private val settings: Settings,
) : InstructorStudioSelectionStorage {
    override fun read(): String? = settings.getStringOrNull(SELECTED_STUDIO_ID_KEY)

    override fun save(studioId: String) {
        settings.putString(SELECTED_STUDIO_ID_KEY, studioId)
    }

    override fun clear() {
        settings.remove(SELECTED_STUDIO_ID_KEY)
    }

    private companion object {
        const val SELECTED_STUDIO_ID_KEY = "instructor.selected_studio_id"
    }
}

class InMemoryInstructorStudioSelectionStorage : InstructorStudioSelectionStorage {
    private var selectedStudioId: String? = null

    override fun read(): String? = selectedStudioId

    override fun save(studioId: String) {
        selectedStudioId = studioId
    }

    override fun clear() {
        selectedStudioId = null
    }
}
