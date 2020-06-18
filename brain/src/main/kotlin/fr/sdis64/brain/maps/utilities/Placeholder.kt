package fr.sdis64.brain.maps.utilities

import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories

fun savePlaceholderImage(filepath: Path, width: Int, height: Int) {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).apply {
        with(createGraphics()) {
            color = Color(0, 0, 0, 0)
            drawRect(0, 0, width, height)
        }
    }
    filepath.parent?.createDirectories()
    ImageIO.write(img, "PNG", filepath.toFile())
}
