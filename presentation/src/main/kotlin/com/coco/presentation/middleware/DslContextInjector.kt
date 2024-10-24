package com.coco.presentation.middleware

import com.coco.infra.dao.DSL_CONTEXT_KEY
import org.jooq.DSLContext
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(0)
class DslContextInjector(
    val dslContext: DSLContext,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        // https://huisam.tistory.com/entry/webflux-coroutine
        return chain.filter(exchange).contextWrite {
            it.put(DSL_CONTEXT_KEY, dslContext)
        }
    }
}
