package com.coco.presentation.configuration

import com.coco.infra.dao.DatabaseCredential
import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class FlywayConfiguration(
    private val env: Environment,
) {
    @Bean(initMethod = "migrate")
    fun flyway(databaseCredential: DatabaseCredential): Flyway =
        databaseCredential.run {
            Flyway(
                Flyway
                    .configure()
                    .dataSource(
                        "jdbc:$engine://$endpoint:$port/$schema?serverTimezone=UTC+9&characterEncoding=UTF-8",
                        username,
                        password,
                    ).baselineOnMigrate(true)
                    .baselineVersion("0")
                    .failOnMissingLocations(true)
                    .locations(env.getRequiredProperty("spring.flyway.location")),
            )
        }
}
