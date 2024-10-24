package com.coco.presentation.middleware

import kotlinx.coroutines.reactor.ReactorContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import kotlin.coroutines.coroutineContext

const val REACTIVE_REQUEST_CONTEXT_KEY = "REACTIVE_REQUEST_CONTEXT_KEY"

@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class ReactiveRequestContextInjector : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        return chain.filter(exchange).contextWrite {
            it.put(REACTIVE_REQUEST_CONTEXT_KEY, exchange.request)
        }
    }
}

suspend fun currentRequest(): ServerHttpRequest {
    return coroutineContext[ReactorContext.Key]?.context?.get(REACTIVE_REQUEST_CONTEXT_KEY) as ServerHttpRequest
}
