package com.coco.presentation.configuration

import com.coco.infra.webclient.ApiUtilsFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebClientConfiguration(
    private val apiUtilsFactory: ApiUtilsFactory,
) {
    @Bean
    fun apiUtils() = apiUtilsFactory.create()
}
