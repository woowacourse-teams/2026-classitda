package com.pheeeew.domain.exception

sealed class ApiException(
    val code: String,
    override val message: String,
) : Exception(message) {
    class InvalidRequest(code: String, message: String) :
            ApiException(code, message)

    class Unauthorized(code: String, message: String) :
            ApiException(code, message)

    class Forbidden(code: String, message: String) :
            ApiException(code, message)

    class NotFound(code: String, message: String) :
            ApiException(code, message)

    class Conflict(code: String, message: String) :
            ApiException(code, message)

    class Unknown(code: String, message: String) :
            ApiException(code, message)

    class Network(code: String, message: String) :
            ApiException(code, message)
}
