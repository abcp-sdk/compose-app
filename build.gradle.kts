plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

group = "com.agent"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(kotlin("stdlib"))
                implementation("com.agent:agent-sdk-kotlin:0.1.0")
                implementation("com.google.protobuf:protobuf-javalite:4.34.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.agent.app.MainKt"
    }
}
