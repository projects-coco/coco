package com.coco.presentation.annotation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootApplication
@EnableCocoApplication
annotation class CocoBatchApplication(
    @Suppress("unused")
    @get:AliasFor(
        annotation = SpringBootApplication::class,
        attribute = "exclude",
    ) val exclude: Array<KClass<*>> = [
        R2dbcAutoConfiguration::class,
        JooqAutoConfiguration::class,
        JdbcClientAutoConfiguration::class,
    ],
)
