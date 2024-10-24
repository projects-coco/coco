package com.coco.domain.core

import kotlinx.coroutines.reactor.ReactorContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import java.util.function.Consumer
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

interface EventNotification

typealias EventNotificationSinks = Sinks.Many<EventNotification>

inline fun <reified T : EventNotification> Flux<EventNotification>.handleEvent(handler: Consumer<T>): Flux<T> =
    this
        .subscribeOn(Schedulers.boundedElastic())
        .filter { it is T }
        .map { it as T }
        .doOnNext(handler)

class EventNotificationBus(
    private val eventNotificationSinks: EventNotificationSinks = Sinks.many().replay().latest(),
) {
    fun notifications(): Flux<EventNotification> = eventNotificationSinks.asFlux()

    fun emitNotification(event: EventNotification): Sinks.EmitResult = eventNotificationSinks.tryEmitNext(event)
}

data class EventNotificationBusContext(
    val eventNotificationBus: EventNotificationBus,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<EventNotificationBusContext>
}

const val EVENT_NOTIFICATION_BUS_KEY = "EVENT_NOTIFICATION_BUS"

suspend fun currentEventNotificationBus(): EventNotificationBus =
    coroutineContext[EventNotificationBusContext]?.eventNotificationBus
        ?: coroutineContext[ReactorContext.Key]?.context?.get(EVENT_NOTIFICATION_BUS_KEY) as? EventNotificationBus
        ?: throw IllegalStateException("Could not found EventNotificationBus")
