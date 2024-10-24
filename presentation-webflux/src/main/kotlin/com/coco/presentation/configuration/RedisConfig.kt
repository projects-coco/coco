package com.coco.presentation.configuration

import org.redisson.Redisson
import org.redisson.api.RedissonReactiveClient
import org.redisson.client.codec.StringCodec
import org.redisson.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class RedisConfig {
    @Bean
    fun redisClient(env: Environment): RedissonReactiveClient {
        val redisHost = env.getRequiredProperty("app.redis.host")
        val redisPort = env.getRequiredProperty("app.redis.port").toInt()

        val config = Config()
        config.useSingleServer().address = "redis://$redisHost:$redisPort"
        config.codec = StringCodec()

        return Redisson.create(config).reactive()
    }
}
