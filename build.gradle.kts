import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "coco"

@Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dep.management) apply false
}

group = "coco"
version = "1.2.3-RELEASE"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

val lib = libs

subprojects {

    apply {
        plugin("java")
        plugin(
            lib.plugins.kotlin.jvm
                .get()
                .pluginId,
        )
        plugin(
            lib.plugins.kotlinter
                .get()
                .pluginId,
        )
        plugin(
            lib.plugins.maven.publish
                .get()
                .pluginId,
        )
        plugin(
            lib.plugins.java.library
                .get()
                .pluginId,
        )
    }

    java.sourceCompatibility = JavaVersion.VERSION_21
    java.targetCompatibility = JavaVersion.VERSION_21

    repositories {
        mavenCentral()
        maven {
            url = uri(properties["nexus.repo.url.snapshot"] as String)
            credentials {
                username = properties["nexus.repo.user"] as String
                password = properties["nexus.repo.password"] as String
            }
        }
        maven {
            url = uri(properties["nexus.repo.url.release"] as String)
            credentials {
                username = properties["nexus.repo.user"] as String
                password = properties["nexus.repo.password"] as String
            }
        }
    }

    dependencies {
        implementation(lib.kotlin.reflect)
        implementation(lib.kotlinx.coroutines.core)
        implementation(lib.kotlinx.coroutines.reactor)
        implementation(lib.arrow.core)
        implementation("io.netty:netty-resolver-dns-native-macos:4.1.68.Final:osx-aarch_64")

        testImplementation(lib.arrow.core)
        testImplementation(lib.bundles.kotest)
        testImplementation(lib.mockk)
        testImplementation(kotlin("test"))
    }

    tasks {
        formatKotlinMain {
            exclude { it.file.path.contains("generated") }
        }
        lintKotlinMain {
            dependsOn("formatKotlinMain")
            exclude { it.file.path.contains("generated") }
        }

        test {
            useJUnitPlatform()
        }

        compileKotlin {
            compilerOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

        task<Jar>("sourcesJar") {
            enabled = true
            archiveClassifier.set("sources")
            from(sourceSets.getByName("main").allSource)
        }
    }
}

tasks {
    formatKotlinMain {
        exclude { it.file.path.contains("generated") }
    }
    lintKotlinMain {
        dependsOn("formatKotlinMain")
        exclude { it.file.path.contains("generated") }
    }
    build {
        dependsOn("lintKotlinMain")
    }
    test {
        useJUnitPlatform()
    }
}
