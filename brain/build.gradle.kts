import fr.sdis64.supportedEnvironments

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("plugin.serialization")
    kotlin("kapt") // Spring @ConfigurationProperties annotation processing
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("ctac-service-plugin")
    id("ctac-configuration-encryption-plugin")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":api"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.boot:spring-boot-properties-migrator")

    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.ldap:spring-ldap-core")
    implementation("org.springframework.security:spring-security-ldap")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.jasypt.spring.boot)

    implementation("org.postgresql:postgresql:42.3.8")

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.micrometer.registry.prometheus)

    implementation("org.hildan.chrome:chrome-devtools-kotlin:4.3.0-1075693")

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.java)

    implementation("com.github.loki4j:loki-logback-appender:1.4.0")

    implementation("org.jetbrains.lets-plot:lets-plot-common:3.1.0")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.3.0")

    testImplementation(project(":client"))
    testImplementation(kotlin("test"))

    testImplementation("io.mockk:mockk:1.13.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core") // we use mockk instead
    }

    val byteBuddyVersion = "1.12.22"
    // mockk was failing because its mockk dep was overridden by Spring
    testImplementation("net.bytebuddy:byte-buddy:$byteBuddyVersion")
    // mockk was failing because its mockk dep was overridden by Spring
    testImplementation("net.bytebuddy:byte-buddy-agent:$byteBuddyVersion")

    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")

    // for the embedded test LDAP server
    testImplementation("com.unboundid:unboundid-ldapsdk:6.0.7")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.check {
    dependsOn(":api:check")
    dependsOn(":client:check")
}

tasks.test {
    useJUnitPlatform()
}

tasks.bootJar {
    dependsOn(tasks.check)
}

configurationEncryption {
    val filename = "configuration-encryption-key"
    privateKeyFile = project.layout.projectDirectory.file(filename)
    publicKeyFile = project.layout.projectDirectory.file("$filename.pub")
}

val prepareTask = tasks.register<Sync>("dockerPrepare") {
    dependsOn(tasks.bootJar)
    group = "package"

    destinationDir = project.buildDir.resolve("docker")
    from(
        tasks.bootJar.map { it.outputs.files.singleFile },
        file("Dockerfile"),
    )
}

ctacService {
    name = project.name
    environments {
        supportedEnvironments.forEach { envName ->
            environment(envName) {
                dockerPrepareTask = prepareTask
                dockerSwarmServiceFile = file("${project.name}.$envName.service.yml")
            }
        }
    }
}
