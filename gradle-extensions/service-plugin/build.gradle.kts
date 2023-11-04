plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("ctac.repositories-conventions")
}

gradlePlugin {
    plugins {
        register("ctacServicePlugin") {
            id = "ctac-service-plugin"
            implementationClass = "fr.sdis64.CtacServicePlugin"
        }
    }
}
