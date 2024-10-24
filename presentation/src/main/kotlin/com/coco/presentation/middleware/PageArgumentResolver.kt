package com.coco.presentation.middleware

import com.coco.domain.core.PageRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class PageArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == PageRequest::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange,
    ): Mono<Any> {
        val queryStrings = exchange.request.queryParams
        val pageNumber = queryStrings["pageNumber"]?.get(0)?.toInt() ?: 1
        val pageSize = queryStrings["pageSize"]?.get(0)?.toInt() ?: 30
        val sort: String = queryStrings["sort"]?.getOrNull(0) ?: ""
        return Mono.just(
            PageRequest(
                pageSize = pageSize,
                pageNumber = pageNumber,
                sort = PageRequest.Sort.of(sort),
            ),
        )
    }
}
