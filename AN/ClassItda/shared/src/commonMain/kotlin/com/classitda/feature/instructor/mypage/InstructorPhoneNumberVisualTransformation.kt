package com.classitda.feature.instructor.mypage

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

internal const val INSTRUCTOR_PHONE_MAX_DIGITS = 11

internal fun instructorPhoneNumberDigits(phoneNumber: String): String =
    phoneNumber.filter(Char::isDigit).take(INSTRUCTOR_PHONE_MAX_DIGITS)

internal fun formatInstructorPhoneNumber(phoneNumber: String): String {
    val digits = instructorPhoneNumberDigits(phoneNumber)
    if (digits.length <= 3) return digits

    return if (digits.startsWith("02")) {
        when {
            digits.length <= 6 -> "${digits.take(2)}-${digits.drop(2)}"
            digits.length <= 9 -> "${digits.take(2)}-${digits.substring(2, 5)}-${digits.drop(5)}"
            else -> "${digits.take(2)}-${digits.substring(2, 6)}-${digits.drop(6)}"
        }
    } else {
        when {
            digits.length <= 7 -> "${digits.take(3)}-${digits.drop(3)}"
            else -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.drop(7)}"
        }
    }
}

internal object InstructorPhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = instructorPhoneNumberDigits(text.text)
        val formatted = formatInstructorPhoneNumber(digits)

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping =
                object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int =
                        formatInstructorPhoneNumber(digits.take(offset.coerceIn(0, digits.length))).length

                    override fun transformedToOriginal(offset: Int): Int =
                        formatted.take(offset.coerceIn(0, formatted.length)).count(Char::isDigit)
                },
        )
    }
}
