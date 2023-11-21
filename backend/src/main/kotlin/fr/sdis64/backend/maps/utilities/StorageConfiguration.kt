package fr.sdis64.backend.maps.utilities

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import kotlin.io.path.Path

@ConfigurationProperties(prefix = "ctac.maps.storage")
data class StorageConfiguration(
    val mapsDirectoryPath: String,
) {
    fun pathToMapFile(filename: String): Path = Path("$mapsDirectoryPath/${filename}")
}
