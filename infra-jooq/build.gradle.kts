group = "coco.framework"

plugins {
    `java-test-fixtures`
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway.gradle)
}

dependencies {
    api(projects.domain)
    api(libs.r2dbc.mariadb) {
        dependencies {
            implementation("io.projectreactor.netty:reactor-netty-http:1.1.20")
        }
    }
    api(libs.r2dbc.pool)
    api(libs.jwt)
    api(libs.bundles.jooq)
    api(libs.flyway.core)
    api(libs.flyway.mysql)
    api(libs.mariadb.client)

    testImplementation(projects.domain)
    testApi(libs.kotlinx.datetime)

    testFixturesApi(libs.testcontainer.mariadb)
    testFixturesApi(libs.testcontainer.r2dbc)
    testFixturesApi(testFixtures(projects.domain))
    testFixturesApi(libs.bundles.kotest)
    testFixturesApi(libs.apache.configuration2)
    testFixturesApi(libs.snakeyaml)

    jooqGenerator(libs.mariadb.client)
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl = uri(properties["nexus.repo.url.release"] as String)
            val snapshotsRepoUrl = uri(properties["nexus.repo.url.snapshot"] as String)
            credentials {
                username = properties["nexus.repo.user"] as String
                password = properties["nexus.repo.password"] as String
            }
            url = if (parent!!.version.toString().endsWith("RELEASE")) releasesRepoUrl else snapshotsRepoUrl
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            groupId = groupId
            artifactId = "infra"
            version = parent!!.version.toString()
        }
    }
}

val databaseUrl = "jdbc:mariadb://localhost:3306"
val databaseUser = "sample"
val databasePassword = "sample"
val databaseSchema = "sample"
val migrationTable = "flyway_schema_history"

jooq {
    configurations {

        create("main") {
            // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(false)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.mariadb.jdbc.Driver"
                    url = databaseUrl
                    user = databaseUser
                    password = databasePassword
                    properties =
                        listOf(
                            org.jooq.meta.jaxb.Property().apply {
                                key = "PAGE_SIZE"
                                value = "2048"
                            },
                        )
                }

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.jdbc.JDBCDatabase"
                        inputSchema = databaseSchema
                        includes = ".*"
                        excludes = "$migrationTable|deleted_at"
                        isIncludeExcludeColumns = true
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = false
                        isFluentSetters = true
                        isKotlinNotNullRecordAttributes = true
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullInterfaceAttributes = true
                        isKotlinDefaultedNullablePojoAttributes = false
                        isKotlinDefaultedNullableRecordAttributes = false
                    }
                    target.apply {
                        packageName = "com.jooq"
                        directory = "src/generated/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks {
    named("generateJooq").configure {}
}
