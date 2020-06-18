import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    val kotlinVersion = "1.8.20"
    kotlin("js") version kotlinVersion apply false
    kotlin("jvm") version kotlinVersion apply false
    kotlin("multiplatform") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.jpa") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
    kotlin("kapt") version kotlinVersion apply false
    id("org.springframework.boot") version "3.0.2" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    id("org.jetbrains.compose") version "1.4.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<AbstractTestTask> {
        testLogging {
            events(TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR)
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStackTraces = true
        }
    }
}
