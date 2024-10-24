plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "coco"
include(
    "presentation",
    "presentation-core",
    "presentation-batch",
    "domain",
    "infra",
    "infra-webclient",
    "infra-redis",
)
