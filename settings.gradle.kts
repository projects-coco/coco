plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "coco"
include(
    "presentation-core",
    "presentation-webflux",
    "presentation-batch",
    "domain",
    "infra-jooq",
    "infra-webclient",
    "infra-redis",
)
