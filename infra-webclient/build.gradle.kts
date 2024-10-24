group = "coco.framework"

plugins {
    `java-test-fixtures`
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway.gradle)
}

dependencies {
    api(projects.domain)
    implementation(libs.spring.boot.webflux)
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
            artifactId = "infra-webclient"
            version = parent!!.version.toString()
        }
    }
}
