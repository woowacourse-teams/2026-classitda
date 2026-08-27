package com.classitda.feature.instructor.mypage

import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft
import com.classitda.domain.repository.instructor.mypage.StudioList
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.contract.MemberListUiModel
import com.classitda.feature.instructor.mypage.contract.MemberUiModel
import com.classitda.feature.instructor.mypage.contract.StudioImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioImageUiModel
import com.classitda.feature.instructor.mypage.contract.StudioInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioListUiModel
import com.classitda.feature.instructor.mypage.contract.StudioUiModel

internal fun ManagedMember.toMemberUiModel(): MemberUiModel =
    MemberUiModel(
        id = id,
        name = name,
        phoneNumber = maskPhoneNumber(phoneNumber),
        avatarFallback = name.trim().firstOrNull()?.uppercase() ?: "?",
        avatarImageReference = profileImageUrl,
    )

internal fun MemberListPage.toMemberListUiModel(): MemberListUiModel =
    MemberListUiModel(
        totalCount = totalCount,
        members = members.map(ManagedMember::toMemberUiModel),
    )

internal fun MemberRegistrationDraft.toMemberInputUiModel(): MemberInputUiModel =
    MemberInputUiModel(name = name, phoneNumber = phoneNumber)

internal fun ManagedMember.toMemberInputUiModel(): MemberInputUiModel =
    MemberInputUiModel(name = name, phoneNumber = phoneNumber, phoneNumberEditable = !registered)

internal fun MemberInputUiModel.toMemberRegistrationDraft(): MemberRegistrationDraft =
    MemberRegistrationDraft(name = name, phoneNumber = phoneNumber)

internal fun ManagedStudio.toStudioUiModel(): StudioUiModel =
    StudioUiModel(
        id = id,
        name = name,
        address = address,
        image = image?.let(::StudioImageUiModel),
        phoneNumber = formatPhoneNumber(phoneNumber),
        description = description,
        openingTime = openingTime,
        closingTime = closingTime,
    )

internal fun StudioList.toStudioListUiModel(): StudioListUiModel =
    StudioListUiModel(
        totalCount = totalCount,
        studios = studios.map(ManagedStudio::toStudioUiModel),
    )

internal fun ManagedStudio.toStudioInputUiModel(): StudioInputUiModel =
    StudioInputUiModel(
        image = image?.let(::StudioImageInputUiModel),
        name = name,
        address = address,
        phoneNumber = phoneNumber,
        description = description,
        openingTime = openingTime,
        closingTime = closingTime,
    )

internal fun StudioInputUiModel.toStudioRegistrationDraft(): StudioRegistrationDraft =
    StudioRegistrationDraft(
        image = image?.selection,
        name = name,
        address = address,
        phoneNumber = phoneNumber,
        description = description,
        openingTime = openingTime,
        closingTime = closingTime,
    )

private fun maskPhoneNumber(phoneNumber: String): String {
    val digits = phoneNumber.filter(Char::isDigit)
    return when {
        digits.length == 11 -> "${digits.take(3)}-****-${digits.takeLast(4)}"
        digits.length == 10 -> "${digits.take(3)}-***-${digits.takeLast(4)}"
        else -> phoneNumber
    }
}

private fun formatPhoneNumber(phoneNumber: String): String {
    val digits = phoneNumber.filter(Char::isDigit)
    return when {
        digits.length == 11 -> {
            "${digits.take(3)}-${digits.substring(3, 7)}-${digits.takeLast(4)}"
        }

        digits.length == 10 && digits.startsWith("02") -> {
            "${digits.take(2)}-${digits.substring(2, 6)}-${digits.takeLast(4)}"
        }

        digits.length == 9 && digits.startsWith("02") -> {
            "${digits.take(2)}-${digits.substring(2, 5)}-${digits.takeLast(4)}"
        }

        digits.length == 10 -> {
            "${digits.take(3)}-${digits.substring(3, 6)}-${digits.takeLast(4)}"
        }

        else -> {
            phoneNumber
        }
    }
}
