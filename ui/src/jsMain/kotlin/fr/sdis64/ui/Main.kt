package fr.sdis64.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import app.softwork.routingcompose.BrowserRouter
import fr.sdis64.client.CtacClient
import fr.sdis64.ui.admin.ManualIndicatorAdmin
import fr.sdis64.ui.admin.MountainRescueAdmin
import fr.sdis64.ui.auth.LoginPage
import fr.sdis64.ui.auth.authenticatedRoute
import fr.sdis64.ui.overflow.FullscreenOverflowToggler
import fr.sdis64.ui.screens.Codis
import fr.sdis64.ui.screens.Crisis
import fr.sdis64.ui.screens.Cta
import fr.sdis64.ui.screens.Samu
import fr.sdis64.ui.utilities.TableStylesheet
import kotlinx.coroutines.launch
import kotlinx.datetime.internal.JSJoda.Clock
import kotlinx.datetime.internal.JSJoda.Instant
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.renderComposable

fun resolveCtacClient(): CtacClient = CtacClient(js("CTAC_API_URL") as String)

fun main() {
    monkeyPatchFetchForCredentials()

    val client = resolveCtacClient()
    val uiSpawnedAt = Instant.now(Clock.systemUTC())

    renderComposable(rootElementId = "root") {
        val scope = rememberCoroutineScope()
        scope.launch {
            try {
                client.recoverSession()
            } catch (e: Throwable) {
                // catching Throwable because 502 Gateway failures are not reported as Exception and will break the app
                console.error(e)
            }
        }

        BrowserRouter(initPath = "/") {
            // TODO: find a way to define this type of nested route, without hardcoding the different segment of the route
            route("/screen") {
                ScreenRefresher(client, uiSpawnedAt)
                route("/codis") {
                    Style(DarkThemeStylesheet)
                    Style(ScreenStylesheet)
                    FullscreenOverflowToggler()
                    Codis(client)
                }
                route("/cta") {
                    Style(DarkThemeStylesheet)
                    Style(ScreenStylesheet)
                    FullscreenOverflowToggler()
                    Cta(client)
                }
                route("/crisis") {
                    Style(DarkThemeStylesheet)
                    Style(ScreenStylesheet)
                    Crisis()
                }
                route("/samu") {
                    Samu(client)
                }
            }
            route("/admin-dashboard") {
                // TODO: remove usage of that redirectTo field, it should be infered somehow
                authenticatedRoute("/manual-indicators", "/admin-dashboard/manual-indicators", client) {
                    StandardPage(client) {
                        ManualIndicatorAdmin(client)
                    }
                }
                authenticatedRoute("/organisms", "/admin-dashboard/organisms", client) {
                    StandardPage(client) {
                        MountainRescueAdmin(client)
                    }
                }
            }
            route(Routes.LOGIN.path) {
                StandardPage(client) {
                    LoginPage(client)
                }
            }
            noMatch {
                redirect(Routes.LOGIN.path)
            }
        }
        Style(CtacStylesheet)
        Style(TableStylesheet)
    }
}

private object CtacStylesheet : StyleSheet() {
    init {
        "body" style {
            fontFamily(
                "Helvetica Neue",
                "Helvetica",
                "Arial",
                "Lucida Grande",
                "sans-serif",
            )
            margin(0.px)
        }
    }
}

private object ScreenStylesheet : StyleSheet() {
    init {
        "body" style {
            fontSize(25.px)
        }
    }
}

@Composable
private fun StandardPage(client: CtacClient, block: @Composable () -> Unit) {
    CtacHeader(client)
    Main(attrs = {
        style {
            property("margin-right", auto)
            property("margin-left", auto)
            val padding = 16.px
            paddingRight(padding)
            paddingLeft(padding)
            maxWidth(1280.px)
        }
    }) {
        block()
    }
}
