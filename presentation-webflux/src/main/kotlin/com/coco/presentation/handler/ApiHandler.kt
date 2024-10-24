package com.coco.presentation.handler

import arrow.core.Either
import arrow.core.Option
import arrow.core.raise.Effect
import arrow.core.raise.Raise
import arrow.core.raise.fold
import arrow.core.toOption
import com.coco.domain.core.ValidType
import com.coco.presentation.middleware.currentRequest
import kotlinx.coroutines.withTimeout
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.reflect.KProperty0
import kotlin.time.Duration

sealed class ApiError(
    val status: HttpStatus,
    val body: Body,
) {
    class ApiException(
        val error: ApiError,
    ) : RuntimeException()

    data class Body(
        var field: String? = null,
        var error: String? = null,
        var exceptionMessage: String? = null,
        var exceptionClass: String? = null,
    )

    data class ValidationFailed(
        val field: String,
        val error: String,
    ) : ApiError(
            HttpStatus.BAD_REQUEST,
            Body(field, error),
        )

    data class BadRequest(
        val error: String,
    ) : ApiError(HttpStatus.BAD_REQUEST, Body(error = error))

    data object Unauthorized : ApiError(HttpStatus.UNAUTHORIZED, Body())

    data class Forbidden(
        val error: String,
    ) : ApiError(HttpStatus.FORBIDDEN, Body(error = error))

    data class NotFound(
        val error: String,
    ) : ApiError(HttpStatus.NOT_FOUND, Body(error = error))

    data class InternalServerError(
        val error: String,
    ) : ApiError(HttpStatus.INTERNAL_SERVER_ERROR, Body(error = error))
}

private suspend fun <A> retrieveRequestInfo(handler: Effect<ApiError, A>): Triple<ServerHttpRequest, List<Any>, Any> {
    val request: ServerHttpRequest = currentRequest()
    val parameters: List<Any> =
        handler.javaClass.declaredFields
            .slice(3..<handler.javaClass.declaredFields.size)
            .map { field ->
                field.trySetAccessible()
                field.get(handler)
            }.filter { value: Any? -> Objects.nonNull(value) }
            .filter { value: Any? ->
                value !is ServerHttpRequest && value !is ServerHttpResponse && value !is Continuation<*>
            }.toList()
    val remoteAddress: Any = request.headers["X-Forwarded-For"] ?: request.remoteAddress ?: "unknown"
    return Triple(request, parameters, remoteAddress)
}

suspend fun <A : ResponseEntity<*>> response(handler: Effect<ApiError, A>): A = handler.fold({ throw ApiError.ApiException(it) }, { it })

suspend fun <A> handle(
    successCode: HttpStatus = HttpStatus.OK,
    timeout: Duration = Duration.INFINITE,
    handler: Effect<ApiError, A>,
): ResponseEntity<A> {
    var result: ResponseEntity<A>? = null
    withTimeout(timeout) {
        handler
            .fold({ throw ApiError.ApiException(it) }, { result = ResponseEntity.status(successCode).body(it) })
    }
    if (result == null) {
        throw ApiError.ApiException(ApiError.InternalServerError("알 수 없는 오류가 발생하였습니다. 지속 발생 시, 고객센터로 문의해주세요."))
    } else {
        return result!!
    }
}

context(Raise<ApiError>)
fun <A> Either<String, A>.bindOrFail(field: String): A = mapLeft { ApiError.ValidationFailed(field, it) }.bind()

context(Raise<ApiError>)
fun <V, A> KProperty0<V>.validate(validType: ValidType<V, A>): A = validType.validate(invoke()).bindOrFail(name)

context(Raise<ApiError>)
fun <V, A> KProperty0<V?>.validateToOption(validType: ValidType<V, A>): Option<A> =
    invoke().toOption().map { validType.validate(it).bindOrFail(name) }

context(Raise<ApiError>)
fun raiseBadRequest(error: String): Nothing = raise(ApiError.BadRequest(error))

context(Raise<ApiError>)
fun raiseNotFound(error: String): Nothing = raise(ApiError.NotFound(error))

context(Raise<ApiError>)
fun raiseForbidden(error: String): Nothing = raise(ApiError.Forbidden(error))

context(Raise<ApiError>)
fun raiseInternalServerError(error: String): Nothing = raise(ApiError.InternalServerError(error))
