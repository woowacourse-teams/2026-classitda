package com.pheeeew.core.permission

/** 운영체제 설정 앱에서 Pheeeew의 앱별 설정 화면을 엽니다. */
interface LocationPermissionSettingsLauncher {
    suspend fun openAppSettings(): Boolean
}
