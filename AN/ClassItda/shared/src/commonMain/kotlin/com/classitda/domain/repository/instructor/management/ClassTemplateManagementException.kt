package com.classitda.domain.repository.instructor.management

sealed class ClassTemplateManagementException(
    val code: String,
    message: String,
) : Exception(message) {
    class InvalidRequest(
        code: String,
        message: String,
    ) : ClassTemplateManagementException(code, message)

    class Unauthorized(
        code: String,
        message: String,
    ) : ClassTemplateManagementException(code, message)

    class Forbidden(
        code: String,
        message: String,
    ) : ClassTemplateManagementException(code, message)

    class NotFound(
        code: String,
        message: String,
    ) : ClassTemplateManagementException(code, message)

    class Conflict(
        code: String,
        message: String,
    ) : ClassTemplateManagementException(code, message)

    class Unknown(
        code: String,
        message: String,
    ) : ClassTemplateManagementException(code, message)

    class Network(
        code: String,
        message: String,
    ) : ClassTemplateManagementException(code, message)
}
