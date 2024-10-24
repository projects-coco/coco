package com.coco.domain.core

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

class DumbCocoCoroutineScopeProvider(eventNotificationBus: EventNotificationBus) : CocoCoroutineScopeProvider {
    private val context =
        EmptyCoroutineContext
            .plus(EventNotificationBusContext(eventNotificationBus))

    override fun provide(): CoroutineScope = CoroutineScope(context)
}
