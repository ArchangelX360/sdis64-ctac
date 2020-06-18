package fr.sdis64.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

const val SVG_NAMESPACE = "http://www.w3.org/2000/svg"

internal fun String.toComposeStyle(
    overrides: Map<String, String> = emptyMap(), additionalProps: Map<String, String> = emptyMap()
): String {
    val props = this.split(";").filter { it.isNotEmpty() }.map {
        val (propName, propValue) = it.split(':')
        val override = overrides[propName]
        val value = override ?: "\"$propValue\""
        styleProperty(propName, value)
    }
    val allProps = props + additionalProps.map { styleProperty(it.key, it.value) }
    return """
        style {
${allProps.joinToString("\n").prependIndent("            ")}
        }
    """.trimIndent()
}

private fun styleProperty(name: String, value: String): String = """property("$name", $value)"""

@Serializable
@XmlSerialName("svg", SVG_NAMESPACE, "")
internal data class UsualCtacSvg(
    val viewBox: String,
    val height: Int,
    val width: Int,
    @XmlElement(value = true) val g: SvgG,
) {
    fun toCompose(): String {
        return """
            Svg(attrs = {
                xmlns("$SVG_NAMESPACE")
                height($height)
                width($width)
                viewBox("$viewBox")
            }) {
${g.toCompose().prependIndent("                ")}
            }
        """.trimIndent()
    }

    fun toComposeFunction(name: String, visibility: String?): String {
        return """
        @OptIn(ExperimentalComposeWebSvgApi::class)
        @Composable
        ${visibility.let { "$it " }}fun $name() {
${toCompose().prependIndent("            ")}
        }
    """.trimIndent()
    }
}

@Serializable
@XmlSerialName("g")
internal data class SvgG(
    val transform: String?,
    @XmlElement(value = true) val paths: List<SvgPath>,
) {
    fun toCompose(): String {
        return """
            G(attrs = {
                ${transform?.let { """transform("$transform")""" } ?: ""}
            }) {
${paths.joinToString("\n") { it.toCompose() }.prependIndent("                ")}
            }
        """.trimIndent()
    }
}

@Serializable
@SerialName("path")
internal data class SvgPath(
    val d: String,
    val style: String?,
) {
    fun toCompose(): String = if (style != null) {
        """
        Path(
            d = "$d",
            attrs = {
${style.toComposeStyle().prependIndent("                ")}
            },
        )
        """.trimIndent()
    } else {
        """
        Path(d = "$d")
        """.trimIndent()
    }
}
