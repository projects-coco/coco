import org.springframework.boot.gradle.tasks.bundling.BootJar

group = "coco.framework"

plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dep.management)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.spring.boot.starter)
    api(projects.domain)
    api(projects.infraJooq) {
        dependencies {
            api(libs.bundles.jooq)
        }
    }

    api(libs.slf4j)
    api(libs.logback.core)
    api(libs.logback.classic)
    api("net.logstash.logback:logstash-logback-encoder:7.4")

    testApi(libs.spring.mockk)
    testApi(libs.spring.boot.test)
    testApi(libs.kotest.extensions.spring)
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
            groupId = "coco.framework"
            artifactId = "presentation-core"
            version = parent!!.version.toString()
        }
    }
}

tasks {
    named<BootJar>("bootJar") {
        enabled = false
    }
}
