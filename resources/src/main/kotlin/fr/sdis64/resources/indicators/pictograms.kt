package fr.sdis64.resources.indicators

import fr.sdis64.resources.UsualCtacSvg
import nl.adaptivity.xmlutil.serialization.XML
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText

fun Path.writeWeatherPictogramComposableFunction(fromSvgs: List<Path>) {
    val idToSvg = fromSvgs.sorted().associate { file ->
        val svg = XML.decodeFromString(UsualCtacSvg.serializer(), file.readText())
        file.nameWithoutExtension to svg
    }
    writeText(
        """
            /**
             * THIS FILE IS AUTOGENERATED BY `./gradlew :resources:generateUiResourcesComposableSourceFiles`, DO NOT EDIT MANUALLY
             */
            package fr.sdis64.ui.indicators
            
            import androidx.compose.runtime.Composable
            import org.jetbrains.compose.web.ExperimentalComposeWebSvgApi
            import org.jetbrains.compose.web.svg.*
            
            @OptIn(ExperimentalComposeWebSvgApi::class)
            @Composable
            internal fun weatherPictogram(id: Int) = when (id) {
${idToSvg.entries.joinToString("\n\n") { (id, svg) -> "$id -> ${svg.toCompose()}".prependIndent("                ") }}

                else -> error("unknown weather pictogram with id '${'$'}id'")
            }
        """.trimIndent()
    )
}
