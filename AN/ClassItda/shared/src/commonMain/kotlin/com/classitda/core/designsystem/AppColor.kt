package com.classitda.core.designsystem

import androidx.compose.ui.graphics.Color

object AppColor {
    val Black = Color(0xff000000)
    val Gray900 = Color(0xff0A0A0A)
    val Gray600 = Color(0xff5C5C5C)
    val Gray400 = Color(0xff999999)

    val Gray300 = Color(0xFFCDCDCD)
    val Gray200 = Color(0xffE5E5E5)
    val Gray100 = Color(0xffF0F0F0)

    val White = Color(0xffffffff)

    val Dim = Color(0x66000000)

    val Orange = Color(0xffFF7700)
    val OrangeLight = Color(0xFFFFD4B8)

    val Red = Color(0xffEC3C3C)
    val RedLight = Color(0xffFFB8B8)

    val Blue = Color(0xff007AFF)
    val BlueLight = Color(0xffB8E2FF)

    val Green = Color(0xff00C853)
    val GreenLight = Color(0xffD3F9D3)

    val Purple = Color(0xff6C55D9)
    val PurpleLight = Color(0xffEEEAFE)
}

object StuColors {
    val Black = AppColor.Black
    val White = AppColor.White

    val TextPrimary = AppColor.Gray900
    val TextSecondary = AppColor.Gray600
    val TextTertiary = AppColor.Gray400

    val Background = AppColor.Gray900
    val Surface = AppColor.White
    val Divider = AppColor.Gray200
    val DividerStrong = AppColor.Gray300
    val SurfaceVariant = AppColor.Gray100 // 카드 위에 올라가는 연한 회색 배경

    val Dim = AppColor.Dim // 모달 뒷 배경

    val Green = Color(0xff0B8F6F)
    val GreenLight = Color(0xffF1FAF7)

    val Orange = AppColor.Orange
    val OrangeLight = AppColor.OrangeLight

    val Red = AppColor.Red
    val RedLight = AppColor.RedLight
}

object InsColors {
    val Black = AppColor.Black
    val White = AppColor.White

    val TextPrimary = AppColor.Gray900
    val TextSecondary = Color(0xff70727A)
    val TextTertiary = Color(0xffB8B4C2)

    val Background = Color(0xffF5F4F7)
    val Surface = Color(0xFFFFFFFF) // 카드 배경
    val Divider = Color(0xffE7E7E7)
    val DividerStrong = AppColor.Gray300
    val SurfaceVariant = Color(0xffF8F9FA) // 카드 위에 올라가는 연한 회색 배경

    val Dim = AppColor.Dim // 모달 뒷 배경

    val Purple = Color(0xff6C55D9)
    val PurpleLight = Color(0xffEEEAFE)

    val Orange = AppColor.Orange
    val OrangeLight = AppColor.OrangeLight

    val Red = AppColor.Red
    val RedLight = AppColor.RedLight
}
