plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jasypt.spring.boot)
}

gradlePlugin {
    plugins {
        register("ctacConfigurationEncryptionPlugin") {
            id = "ctac-configuration-encryption-plugin"
            implementationClass = "fr.sdis64.CtacConfigurationEncryptionPlugin"
        }
    }
}
