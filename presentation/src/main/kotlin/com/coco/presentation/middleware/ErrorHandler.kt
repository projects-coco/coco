package com.coco.presentation.middleware

import com.coco.domain.utils.logger
import com.coco.presentation.handler.ApiError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorHandler {
    val logger = logger()

    @ExceptionHandler
    fun handle(exception: ApiError.ApiException): ResponseEntity<ApiError.Body> =
        exception.error.let {
            status(it.status).body(it.body)
        }

    @ExceptionHandler
    fun handle(
        exception: Exception,
        request: ServerHttpRequest,
    ): ResponseEntity<ApiError.Body> {
        logger
            .atError()
            .addKeyValue("stack_trace", exception.stackTraceToString())
            .addKeyValue("request_id", request.id)
            .log()

        return status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError.Body(
                error =
                    "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n" +
                        "문제가 지속되면 관리자에게 문의해주세요.",
                exceptionMessage = exception.message,
                exceptionClass = exception.javaClass.name,
            ),
        )
    }
}
