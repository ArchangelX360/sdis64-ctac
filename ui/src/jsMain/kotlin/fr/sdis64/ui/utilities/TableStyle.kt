package fr.sdis64.ui.utilities

import org.jetbrains.compose.web.css.*

object TableStylesheet : StyleSheet() {
    val lightBorderedTable by style {
        property("border-collapse", "collapse")

        "thead" style {
            backgroundColor(rgb(190, 190, 190))
        }

        "th" style {
            property("border-bottom", "1px solid hsla(100%,100%,100%,.12)")
            fontWeight(500)
        }

        "td" style {
            property("border-bottom", "1px solid hsla(100%,100%,100%,.12)")
        }
    }

    val darkBorderedTable by style {
        property("border-collapse", "collapse")

        "thead" style {
            backgroundColor(rgb(99, 99, 99))
        }

        "th" style {
            property("border-bottom", "1px solid hsla(0,0%,100%,.12)")
            fontWeight(500)
        }

        "td" style {
            property("border-bottom", "1px solid hsla(0,0%,100%,.12)")
        }
    }

    val largeRowTable by style {
        "tr" style {
            height(49.px)
        }
    }

    val mediumRowTable by style {
        "tr" style {
            height(41.px)
        }
    }
}
