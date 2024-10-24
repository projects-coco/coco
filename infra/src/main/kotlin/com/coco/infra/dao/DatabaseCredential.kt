package com.coco.infra.dao

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.reactor.ReactorContext
import org.jooq.DSLContext
import org.jooq.conf.Settings
import org.jooq.conf.StatementType
import org.jooq.impl.DSL
import org.jooq.tools.jdbc.JDBCUtils
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

data class DatabaseCredential(
    val driver: String,
    val engine: String,
    val endpoint: String,
    val port: Int,
    val username: String,
    val password: String,
    val schema: String,
) {
    fun createDslContext() =
        this.createConnectionPool().let {
            DSL.using(
                it,
                JDBCUtils.dialect(it),
                Settings()
                    .withQueryTimeout(5)
                    .withRenderCatalog(false)
                    .withRenderSchema(false)
                    .withExecuteLogging(true)
                    .withStatementType(StatementType.STATIC_STATEMENT),
            )
        }

    private fun createConnectionPool(): ConnectionFactory =
        run {
            val connectionFactories =
                ConnectionFactories.get(
                    ConnectionFactoryOptions
                        .builder()
                        .option(ConnectionFactoryOptions.DRIVER, driver)
                        .option(
                            ConnectionFactoryOptions.PROTOCOL,
                            engine,
                        ).option(ConnectionFactoryOptions.HOST, endpoint)
                        .option(ConnectionFactoryOptions.PORT, port)
                        .option(ConnectionFactoryOptions.USER, username)
                        .option(ConnectionFactoryOptions.PASSWORD, password)
                        .option(ConnectionFactoryOptions.DATABASE, schema)
                        .build(),
                )
            connectionFactories
        }
}

data class JooqCoroutineContext(
    val dslContext: DSLContext,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<JooqCoroutineContext>
}

const val DSL_CONTEXT_KEY = "DSL_CONTEXT"

suspend fun currentDslContext(): DSLContext =
    coroutineContext[JooqCoroutineContext]?.dslContext
        ?: coroutineContext[ReactorContext.Key]?.context?.get(DSL_CONTEXT_KEY) as? DSLContext
        ?: throw IllegalStateException("Could not found DSL Context")
