package com.pheeeew.core.permission

/** 플랫폼의 현재 위치 권한을 조회하고 요청하는 계약입니다. */
interface LocationPermissionController {
    suspend fun currentStatus(): LocationPermissionStatus

    suspend fun requestPermission(): LocationPermissionStatus

    /** 앱의 OS 설정(권한) 화면을 엽니다. 권한이 영구 거부돼 재요청이 불가능할 때 사용합니다. */
    fun openAppSettings()
}
