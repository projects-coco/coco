package com.coco.presentation.middleware

import com.coco.domain.core.EVENT_NOTIFICATION_BUS_KEY
import com.coco.domain.core.EventNotificationBus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class EventNotificationBusInjector(val eventNotificationBus: EventNotificationBus) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        return chain.filter(exchange).contextWrite {
            it.put(EVENT_NOTIFICATION_BUS_KEY, eventNotificationBus)
        }
    }
}
