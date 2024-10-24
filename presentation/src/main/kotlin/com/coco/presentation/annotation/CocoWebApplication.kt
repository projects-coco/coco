package com.coco.presentation.annotation

import com.coco.infra.webclient.ApiUtilsFactory
import com.coco.presentation.configuration.*
import com.coco.presentation.middleware.DslContextInjector
import com.coco.presentation.middleware.EventNotificationBusInjector
import com.coco.presentation.middleware.ReactiveRequestContextInjector
import com.coco.presentation.middleware.WebConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootApplication
@EnableCocoApplication
@Import(
    JsonConfiguration::class,
    WebConfig::class,
    FlywayConfiguration::class,
    WebClientConfiguration::class,
    ApiUtilsFactory::class,
    EventNotificationBusInjector::class,
    DslContextInjector::class,
    ReactiveRequestContextInjector::class,
)
annotation class CocoWebApplication(
    @Suppress("unused")
    @get:AliasFor(
        annotation = SpringBootApplication::class,
        attribute = "exclude",
    ) val exclude: Array<KClass<*>> = [
        R2dbcAutoConfiguration::class,
        JooqAutoConfiguration::class,
    ],
)
