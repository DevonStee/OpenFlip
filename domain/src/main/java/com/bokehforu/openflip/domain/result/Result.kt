package com.bokehforu.openflip.domain.result

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(
        val error: DomainError,
        val cause: Throwable? = null
    ) : Result<Nothing>()
}

interface DomainError {
    val code: String
    val message: String?
}
