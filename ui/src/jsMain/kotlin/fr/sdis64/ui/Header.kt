package fr.sdis64.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.softwork.routingcompose.NavLink
import fr.sdis64.client.CtacClient
import fr.sdis64.client.CtacResult
import fr.sdis64.ui.auth.LoginLink
import fr.sdis64.ui.utilities.errorOr
import fr.sdis64.ui.utilities.hexCodeToRgb
import fr.sdis64.ui.utilities.rememberLoadingState
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun CtacHeader(client: CtacClient) {
    Style(NavbarStylesheet)
    val height = 64.px
    Header(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexWrap(FlexWrap.Wrap)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
            minHeight(height)
            padding(0.px, 16.px)
            backgroundColor("#A72920".hexCodeToRgb())
            color(Color.white)
        }
    }) {
        val session by rememberLoadingState { client.currentSession().map { CtacResult.Success(it) } }

        Span(attrs = {
            style {
                height(height)
                lineHeight(height)
                fontSize(20.px)
                fontWeight(600)
                flexGrow(1)
            }
        }) {
            Text("SDIS64 Mur Image v3.0")
        }
        Nav(attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                flexWrap(FlexWrap.Wrap)
                flexGrow(5)
            }
        }) {
            Div(attrs = { style { display(DisplayStyle.Flex) } }) {
                NavLink(
                    attrs = { classes(NavbarStylesheet.navbarLink) }, to = Routes.SCREEN_CODIS.path
                ) { Text("CODIS") }
                NavLink(attrs = { classes(NavbarStylesheet.navbarLink) }, to = Routes.SCREEN_CTA.path) { Text("CTA") }
                NavLink(
                    attrs = { classes(NavbarStylesheet.navbarLink) }, to = Routes.SCREEN_CRISIS.path
                ) { Text("Crise") }
                NavLink(attrs = { classes(NavbarStylesheet.navbarLink) }, to = Routes.SCREEN_SAMU.path) { Text("SAMU") }
            }
            Div(attrs = { style { display(DisplayStyle.Flex) } }) {
                errorOr(session) { s ->
                    NavLink(
                        attrs = {
                            classes(NavbarStylesheet.navbarLink)
                            if (s == null) attr("disabled", "")
                        }, to = Routes.ADMIN_DASHBOARD_MOUNTAIN_RESCUE.path
                    ) { Text("Secours Montagne") }
                    NavLink(
                        attrs = {
                            classes(NavbarStylesheet.navbarLink)
                            if (s == null) attr("disabled", "")
                        }, to = Routes.ADMIN_DASHBOARD_MANUAL_INDICATORS.path
                    ) { Text("Indicateurs manuel") }
                }
            }
            LoginLink(client)
        }
    }
}

internal object NavbarStylesheet : StyleSheet() {
    init {
        "a" style {
            textDecoration("none")
            color(Color.white)
        }
        "a[disabled]" style {
            cursor("not-allowed")
        }
    }

    val navbarLink by style {
        fontSize(16.px)
        color(Color.white)
        fontWeight(600)
        margin(16.px)
        property("touch-action", "manipulation")

        (self + hover) {
            color("#C2C2C4".hexCodeToRgb())
        }
        (self + focus) {
            color("#C2C2C4".hexCodeToRgb())
        }
    }
}
