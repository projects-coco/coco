package com.coco.infra

import com.coco.domain.genString
import com.coco.infra.dao.DatabaseCredential
import com.coco.infra.dao.JooqCoroutineContext
import io.kotest.assertions.fail
import io.kotest.core.TestConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.YAMLConfiguration
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.jdbc.DriverDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.FileInputStream
import javax.sql.DataSource

object DatabaseTestContainer : org.testcontainers.containers.MariaDBContainer<DatabaseTestContainer>("mariadb:10.6") {
    val config = CompositeConfiguration()
    private val TEST_DB_SCHEMA by lazy {
        config.getString("test.db.schema")
    }
    private val TEST_DB_USERNAME by lazy {
        config.getString("test.db.username")
    }
    private val TEST_DB_PASSWORD by lazy {
        config.getString("test.db.password")
    }
    private val TEST_DB_CMD_NAME by lazy {
        config.getString("test.db.cmd-name")
    }
    private val TEST_DB_ENGINE by lazy {
        config.getString("test.db.engine")
    }
    private val TEST_DB_ENDPOINT by lazy {
        config.getString("test.db.endpoint")
    }
    private val TEST_DB_FLYWAY_SQL_LOCATION by lazy {
        config.getString("test.db.flyway-sql-location")
    }
    private val FLYWAY_SCHEMA_HISTORY by lazy {
        config.getString("test.db.flyway-schema-history")
    }

    init {
        val yaml = YAMLConfiguration()
        yaml.read(FileInputStream("src/test/resources/application.yml"))
        config.addConfiguration(yaml)

        withCreateContainerCmdModifier { cmd -> cmd.withName(TEST_DB_CMD_NAME) }
        withReuse(true)
        withStartupAttempts(1)
        withStartupTimeoutSeconds(30)
        withDatabaseName(TEST_DB_SCHEMA)
    }

    private val existContainer by lazy {
        val containers = dockerClient.listContainersCmd().exec()
        containers.firstOrNull {
            it.names.contains(TEST_DB_CMD_NAME) &&
                it.state == "running" &&
                it.ports.any { mapped -> mapped.privatePort == 3306 && mapped.publicPort != null }
        }
    }

    fun TestConfiguration.installDatabase(): DatabaseCredential =
        createSchemaWithMigration()
            .also { credential ->
                System.setProperty("org.jooq.no-logo", "true")
                System.setProperty("org.jooq.no-tips", "true")
                afterTest { truncateTables(credential) } // every test clear rows
                afterSpec { dropSchema(credential) } // drop schema after run tests
                val context = JooqCoroutineContext(credential.createDslContext())
                aroundTest { (case, execution) ->
                    withContext(context) {
                        execution(case)
                    }
                }
            }

    private suspend fun dropSchema(credential: DatabaseCredential) {
        val context = dslContext(credential)
        val meta = context.meta()

        context.dropSchema(meta.getSchemas(credential.schema).first()).awaitFirst()
        // println("complete to drop test schema(${credential.schema})")
    }

    private fun truncateTables(credential: DatabaseCredential) {
        runBlocking(Dispatchers.IO) {
            dslContext(credential).apply {
                val tables =
                    meta()
                        .getSchemas(credential.schema)
                        .first()
                        .tables
                        .filterNot { it.name == FLYWAY_SCHEMA_HISTORY }
                        .toMutableList()
                while (tables.isNotEmpty()) {
                    val table = tables.removeFirst()
                    try {
                        truncate(table).awaitFirst()
                    } catch (e: Exception) {
                        tables.addLast(table)
                    }
                }
            }
        }
        // println("complete to truncate test tables(${credential.schema})")
    }

    private fun createSchemaWithMigration(): DatabaseCredential {
        if (existContainer == null && !isRunning) start()

        return credential().apply {
            val migrate =
                Flyway(
                    Flyway
                        .configure()
                        .baselineOnMigrate(true)
                        .dataSource(jdbcDatasource())
                        .createSchemas(true)
                        .defaultSchema(schema)
                        .baselineVersion("0")
                        .failOnMissingLocations(true)
                        .locations(TEST_DB_FLYWAY_SQL_LOCATION),
                ).migrate()

            if (!migrate.success) fail("Test용 데이터베이스 스키마를 생성하는데 실패했습니다.")
        }
    }

    private fun credential(): DatabaseCredential {
        if (!this.isRunning) {
            fail("Database test container 가 실행되지 않은 상태입니다.")
        }

        return DatabaseCredential(
            driver = "pool",
            endpoint = TEST_DB_ENDPOINT,
            port = getMappedPort(3306),
            username = "root",
            password = "test",
            engine = TEST_DB_ENGINE,
            schema = genString(10) + TEST_DB_SCHEMA,
        )
    }
}

private fun DatabaseCredential.jdbcDatasource(): DataSource =
    DriverDataSource(
        Thread.currentThread().contextClassLoader,
        "org.mariadb.jdbc.Driver",
        "jdbc:$engine://$endpoint:$port",
        username,
        password,
    )

private fun dslContext(credential: DatabaseCredential): DSLContext = DSL.using(credential.jdbcDatasource(), SQLDialect.MARIADB)
