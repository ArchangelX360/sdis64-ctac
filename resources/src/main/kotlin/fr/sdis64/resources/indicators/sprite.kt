package fr.sdis64.resources.indicators

import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

fun cutWeatherPictogramFromSprite(spriteFile: Path, iconDirectory: Path) {
    val image = spriteFile.inputStream().use { ImageIO.read(it) }

    val icons = mapOf(
        1 to Viewport(
            x = 168,
            y = 42,
            width = 58,
            height = 48,
        ),
        2 to Viewport(
            x = 124,
            y = 100,
            width = 62,
            height = 36,
        ),
        3 to Viewport(
            x = 226,
            y = 42,
            width = 51,
            height = 48,
        ),
        4 to Viewport(
            x = 56,
            y = 136,
            width = 60,
            height = 22,
        ),
        5 to Viewport(
            x = 56,
            y = 100,
            width = 68,
            height = 36,
        ),
        6 to Viewport(
            x = 278,
            y = 182,
            width = 22,
            height = 54,
        ),
        7 to Viewport(
            x = 277,
            y = 126,
            width = 25,
            height = 56,
        ),
        8 to Viewport(
            x = 100,
            y = 42,
            width = 68,
            height = 51,
        ),
        9 to Viewport(
            x = 186,
            y = 90,
            width = 68,
            height = 41,
        ),
    )

    icons.forEach { (id, viewport) ->
        val icon = image.getSubimage(viewport.x, viewport.y, viewport.width, viewport.height)
        iconDirectory.resolve("$id.png").outputStream().use { outputStream ->
            ImageIO.write(icon, "PNG", outputStream)
        }
    }
}

private data class Viewport(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
