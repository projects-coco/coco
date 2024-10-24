import org.springframework.boot.gradle.tasks.bundling.BootJar

group = "coco.framework"

plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dep.management)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.presentationCore)
    api(projects.infraRedis)
    api(projects.infraWebclient)
    api(libs.spring.boot.webflux)
    api(libs.jackson.module.kotlin)
    api(libs.kotlinx.serialization.json)

    implementation(libs.apache.poi)
    implementation(libs.apache.poi.ooxml)
    implementation(libs.apache.commons.compress)
    implementation("com.github.ozlerhakan:poiji:4.4.0")
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
            artifactId = "presentation"
            version = parent!!.version.toString()
        }
    }
}

tasks {
    named<BootJar>("bootJar") {
        enabled = false
    }
}
