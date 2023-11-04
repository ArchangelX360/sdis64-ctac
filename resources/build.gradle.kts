plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("ctac.repositories-conventions")
}

dependencies {
    implementation(libs.xmlutil.core.jvm)
    implementation(libs.xmlutil.serialization.jvm)
}

tasks.register<JavaExec>("generateUiResourcesComposableSourceFiles") {
    val uiProject = project(":ui").layout.projectDirectory
    val weatherPictogramSourceFile = uiProject.asFileTree.matchingSingleFile { include("**/WeatherPictograms.kt") }
    val mailIconConstantsSourceFile = uiProject.asFileTree.matchingSingleFile { include("**/MailIconConstants.kt") }
    val vehicleMapSvgSourceFile = uiProject.asFileTree.matchingSingleFile { include("**/VehicleMapSvg.kt") }
    val vigipirateSvgSourceFile = uiProject.asFileTree.matchingSingleFile { include("**/VigipirateIcons.kt") }

    inputs.files(sourceSets.main.map { it.resources })
    outputs.files(
        weatherPictogramSourceFile,
        mailIconConstantsSourceFile,
        vehicleMapSvgSourceFile,
        vigipirateSvgSourceFile,
    )

    classpath = sourceSets.main.map { it.runtimeClasspath }.get()
    mainClass = "fr.sdis64.resources.MainKt"
    systemProperty("ctac.ui.weatherPictogramSourceFile", weatherPictogramSourceFile)
    systemProperty("ctac.ui.mailIconConstantsSourceFile", mailIconConstantsSourceFile)
    systemProperty("ctac.ui.vehicleMapSvgSourceFile", vehicleMapSvgSourceFile)
    systemProperty("ctac.ui.vigipirateSvgSourceFile", vigipirateSvgSourceFile)
}

fun FileTree.matchingSingleFile(patternFilterable: Action<PatternFilterable>): File =
    matching(patternFilterable).files.single()
