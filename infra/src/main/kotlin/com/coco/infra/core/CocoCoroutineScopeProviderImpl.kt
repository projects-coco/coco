package com.coco.infra.core

import com.coco.domain.core.CocoCoroutineScopeProvider
import com.coco.domain.core.EventNotificationBus
import com.coco.domain.core.EventNotificationBusContext
import com.coco.infra.dao.JooqCoroutineContext
import kotlinx.coroutines.CoroutineScope
import org.jooq.DSLContext
import kotlin.coroutines.EmptyCoroutineContext

class CocoCoroutineScopeProviderImpl(
    dslContext: DSLContext,
    eventNotificationBus: EventNotificationBus,
) : CocoCoroutineScopeProvider {
    private val context =
        EmptyCoroutineContext
            .plus(JooqCoroutineContext(dslContext))
            .plus(EventNotificationBusContext(eventNotificationBus))

    override fun provide(): CoroutineScope = CoroutineScope(context = context)
}
