package com.coco.presentation.annotation

import com.coco.infra.core.CocoCoroutineScopeProviderImpl
import com.coco.presentation.configuration.EventNotificationBusConfiguration
import com.coco.presentation.configuration.JooqConfiguration
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(
    EventNotificationBusConfiguration::class,
    CocoCoroutineScopeProviderImpl::class,
    JooqConfiguration::class,
)
annotation class EnableCocoApplication
