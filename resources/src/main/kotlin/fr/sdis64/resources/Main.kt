package fr.sdis64.resources

import fr.sdis64.resources.indicators.writeVigipirateIconsComposableFunction
import fr.sdis64.resources.indicators.writeWeatherPictogramComposableFunction
import fr.sdis64.resources.mail.writeMailIconsDPathConstants
import fr.sdis64.resources.vehicles.writeVehicleMapSvgComposableFunction
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

fun main() {
    val weatherPictogramSourceFile = systemPropertyAsPath("ctac.ui.weatherPictogramSourceFile")
    weatherPictogramSourceFile.writeWeatherPictogramComposableFunction(fromSvgs = svgsOfResourceDirectory("weather-pictograms"))

    val mailIconConstantsSourceFile = systemPropertyAsPath("ctac.ui.mailIconConstantsSourceFile")
    mailIconConstantsSourceFile.writeMailIconsDPathConstants(fromSvgs = svgsOfResourceDirectory("icons"))

    val vehicleMapSvgSourceFile = systemPropertyAsPath("ctac.ui.vehicleMapSvgSourceFile")
    vehicleMapSvgSourceFile.writeVehicleMapSvgComposableFunction(fromSvg = resourceAsPath("vehicle-map.svg"))

    val vigipirateSvgSourceFile = systemPropertyAsPath("ctac.ui.vigipirateSvgSourceFile")
    vigipirateSvgSourceFile.writeVigipirateIconsComposableFunction(fromSvgs = svgsOfResourceDirectory("vigipirate"))
}

private fun systemPropertyAsPath(propertyName: String): Path = Path.of(System.getProperty(propertyName))

private fun resourceAsPath(resourcePath: String): Path {
    val r = ClassLoader.getSystemResource(resourcePath) ?: error("resource $resourcePath not found")
    return Path.of(r.path)
}

private fun svgsOfResourceDirectory(resourceDirectoryPath: String): List<Path> =
    resourceAsPath(resourceDirectoryPath).listDirectoryEntries("*.svg")
