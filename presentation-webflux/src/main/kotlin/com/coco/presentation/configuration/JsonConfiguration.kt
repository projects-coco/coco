package com.coco.presentation.configuration

import com.coco.domain.core.LocalDateTimeSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.datetime.serializers.LocalTimeIso8601Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.serializersModuleOf
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.time.LocalDate as JLocalDate
import java.time.LocalDateTime as JLocalDateTime
import java.time.LocalTime as JLocalTime

private val json =
    Json {
        ignoreUnknownKeys = true
        serializersModule =
            SerializersModule {
                serializersModuleOf(LocalTime::class, LocalTimeIso8601Serializer)
            } +
            SerializersModule {
                serializersModuleOf(LocalDate::class, LocalDateIso8601Serializer)
            } +
            SerializersModule {
                serializersModuleOf(LocalDateTime::class, LocalDateTimeIso8601Serializer)
            } +
            SerializersModule {
                contextual(LocalDateTimeSerializer)
            }
    }

@Configuration
@Import(JacksonConfiguration::class)
class JsonConfiguration : WebFluxConfigurer {
    @Bean
    fun jsonSerializer() = json

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().apply {
            configureDefaultCodec { KotlinSerializationJsonHttpMessageConverter(json) }
            kotlinSerializationJsonEncoder(KotlinSerializationJsonEncoder(json))
            kotlinSerializationJsonDecoder(KotlinSerializationJsonDecoder(json))
        }
    }
}

@Configuration
class JacksonConfiguration {
    @Bean
    fun iso8601Module(): Module {
        val module = SimpleModule()

        module.addSerializer(
            JLocalDate::class.java,
            object : JsonSerializer<JLocalDate?>() {
                @Throws(IOException::class)
                override fun serialize(
                    localDate: JLocalDate?,
                    jsonGenerator: JsonGenerator?,
                    serializerProvider: SerializerProvider?,
                ) {
                    jsonGenerator?.writeString(DateTimeFormatter.ISO_LOCAL_DATE.format(localDate))
                }
            },
        )

        module.addSerializer(
            JLocalTime::class.java,
            object : JsonSerializer<JLocalTime?>() {
                override fun serialize(
                    localTime: JLocalTime?,
                    jsonGenerator: JsonGenerator?,
                    serializerProvider: SerializerProvider?,
                ) {
                    jsonGenerator?.writeString(DateTimeFormatter.ISO_LOCAL_TIME.format(localTime))
                }
            },
        )

        module.addSerializer(
            JLocalDateTime::class.java,
            object : JsonSerializer<JLocalDateTime?>() {
                @Throws(IOException::class)
                override fun serialize(
                    localDateTime: JLocalDateTime?,
                    jsonGenerator: JsonGenerator?,
                    serializerProvider: SerializerProvider?,
                ) {
                    jsonGenerator?.writeString(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime))
                }
            },
        )

        module.addSerializer(
            LocalDate::class.java,
            object : JsonSerializer<LocalDate?>() {
                @Throws(IOException::class)
                override fun serialize(
                    localDate: LocalDate?,
                    jsonGenerator: JsonGenerator?,
                    serializerProvider: SerializerProvider?,
                ) {
                    json.encodeToString(LocalDateIso8601Serializer, localDate ?: return)
                }
            },
        )

        module.addSerializer(
            LocalTime::class.java,
            object : JsonSerializer<LocalTime?>() {
                override fun serialize(
                    localTime: LocalTime?,
                    jsonGenerator: JsonGenerator?,
                    serializerProvider: SerializerProvider?,
                ) {
                    json.encodeToString(LocalTimeIso8601Serializer, localTime ?: return)
                }
            },
        )

        module.addSerializer(
            LocalDateTime::class.java,
            object : JsonSerializer<LocalDateTime?>() {
                @Throws(IOException::class)
                override fun serialize(
                    localDateTime: LocalDateTime?,
                    jsonGenerator: JsonGenerator?,
                    serializerProvider: SerializerProvider?,
                ) {
                    json.encodeToString(LocalDateTimeIso8601Serializer, localDateTime ?: return)
                }
            },
        )

        return module
    }
}
