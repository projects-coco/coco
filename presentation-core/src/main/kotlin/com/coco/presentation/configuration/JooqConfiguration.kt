package com.coco.presentation.configuration

import com.coco.infra.core.JooqAtomicityProvider
import com.coco.infra.dao.DatabaseCredential
import org.jooq.DSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment

@Configuration
@Import(
    JooqAtomicityProvider::class,
)
class JooqConfiguration {
    @Bean
    fun databaseCredential(env: Environment): DatabaseCredential {
        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")
        return DatabaseCredential(
            driver = env.getRequiredProperty("app.database.driver") ?: "pool",
            engine = env.getRequiredProperty("app.database.engine"),
            endpoint = env.getRequiredProperty("app.database.endpoint"),
            port = env.getRequiredProperty("app.database.port").toInt(),
            username = env.getRequiredProperty("app.database.username"),
            password = env.getRequiredProperty("app.database.password"),
            schema = env.getRequiredProperty("app.database.schema"),
        )
    }

    @Bean
    fun dslContext(databaseCredential: DatabaseCredential): DSLContext = databaseCredential.createDslContext()
}
