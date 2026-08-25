package com.classitda.feature.instructor.mypage

import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.instructor.mypage.MemberSortOrder
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.feature.instructor.mypage.contract.FacilityImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityImageUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityListUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityUiModel
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.contract.MemberListUiModel
import com.classitda.feature.instructor.mypage.contract.MemberSortOption
import com.classitda.feature.instructor.mypage.contract.MemberUiModel

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
    MemberInputUiModel(name = name, phoneNumber = phoneNumber)

internal fun MemberInputUiModel.toMemberRegistrationDraft(): MemberRegistrationDraft =
    MemberRegistrationDraft(name = name, phoneNumber = phoneNumber)

internal fun MemberSortOption.toDomain(): MemberSortOrder =
    when (this) {
        MemberSortOption.RECENTLY_REGISTERED -> MemberSortOrder.RECENTLY_REGISTERED
        MemberSortOption.NAME_ASC -> MemberSortOrder.NAME_ASC
    }

internal fun MemberSortOrder.toUiModel(): MemberSortOption =
    when (this) {
        MemberSortOrder.RECENTLY_REGISTERED -> MemberSortOption.RECENTLY_REGISTERED
        MemberSortOrder.NAME_ASC -> MemberSortOption.NAME_ASC
    }

internal fun ManagedFacility.toFacilityUiModel(): FacilityUiModel =
    FacilityUiModel(
        id = id,
        name = name,
        address = address,
        image = image?.let(::FacilityImageUiModel),
        phoneNumber = formatPhoneNumber(phoneNumber),
        description = description,
        openingTime = openingTime,
        closingTime = closingTime,
    )

internal fun FacilityList.toFacilityListUiModel(): FacilityListUiModel =
    FacilityListUiModel(
        totalCount = totalCount,
        facilities = facilities.map(ManagedFacility::toFacilityUiModel),
    )

internal fun ManagedFacility.toFacilityInputUiModel(): FacilityInputUiModel =
    FacilityInputUiModel(
        image = image?.let(::FacilityImageInputUiModel),
        name = name,
        address = address,
        phoneNumber = phoneNumber,
        description = description,
        openingTime = openingTime,
        closingTime = closingTime,
    )

internal fun FacilityInputUiModel.toFacilityRegistrationDraft(): FacilityRegistrationDraft =
    FacilityRegistrationDraft(
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
