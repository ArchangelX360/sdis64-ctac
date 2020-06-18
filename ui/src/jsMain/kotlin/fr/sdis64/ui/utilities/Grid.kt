package fr.sdis64.ui.utilities

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

val physicalTvWidth = 1366.px
val physicalTvHeight = 768.px

@Composable
fun GridTile(row: String, column: String, block: @Composable () -> Unit) {
    Div(
        attrs = {
            style {
                gridRow(row)
                gridColumn(column)
                overflow("hidden")
            }
        },
    ) {
        block()
    }
}

@Composable
fun Tile(title: String, padded: Boolean = false, block: @Composable () -> Unit) {
    Tile({ Text(title) }, padded, block)
}

@Composable
fun Tile(titleBlock: ContentBuilder<HTMLDivElement>? = null, padded: Boolean = false, block: @Composable () -> Unit) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                width(100.percent)
                height(100.percent)
            }
        }
    ) {
        val titleHeight = 44.px
        val titleMargin = 23.px
        if (titleBlock != null) {
            Div(
                attrs = {
                    style {
                        fontSize(35.px)
                        fontWeight(500)
                        height(titleHeight)
                        margin(titleMargin, 5.px)
                    }
                },
                titleBlock,
            )
        }
        Div(
            attrs = {
                style {
                    if (titleBlock != null) {
                        height(100.percent - titleHeight - (titleMargin * 2))
                    } else {
                        height(100.percent)
                    }
                    if (padded) {
                        padding(0.px, 5.px)
                    }
                    fontSize(30.px)
                }
            }
        ) {
            block()
        }
    }
}
