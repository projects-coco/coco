package com.coco.presentation.configuration

import com.coco.domain.core.EventNotificationBus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventNotificationBusConfiguration {
    @Bean
    fun eventNotificationBus() = EventNotificationBus()
}
