group = "coco.framework"

plugins {
    `java-test-fixtures`
}

dependencies {
    api(libs.ulid)
    api(libs.slf4j)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)
    testFixturesApi(libs.mockk)
    testFixturesApi(libs.kotest.property)
    testFixturesApi(libs.kotest.assertions.core)
    testFixturesApi(libs.kotest.assertions.arrow)
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
            artifactId = "domain"
            version = parent!!.version.toString()
        }
    }
}
