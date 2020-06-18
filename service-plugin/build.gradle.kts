plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("ctacServicePlugin") {
            id = "ctac-service-plugin"
            implementationClass = "fr.sdis64.CtacServicePlugin"
        }
    }
}
