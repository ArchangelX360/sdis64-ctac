package fr.sdis64.ui.auth

import androidx.compose.runtime.*
import app.softwork.routingcompose.NavLink
import app.softwork.routingcompose.RouteBuilder
import app.softwork.routingcompose.Router
import app.softwork.routingcompose.Routing
import fr.sdis64.client.CtacClient
import fr.sdis64.client.CtacResult
import fr.sdis64.ui.NavbarStylesheet
import fr.sdis64.ui.Routes
import fr.sdis64.ui.utilities.ErrorMessage
import fr.sdis64.ui.utilities.errorOr
import fr.sdis64.ui.utilities.rememberLoadingState
import kotlinx.browser.window
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.Location
import org.w3c.dom.url.URL

private const val redirectQueryParameter = "redirectTo"

@Routing
@Composable
fun RouteBuilder.authenticatedRoute(
    route: String,
    redirectTo: String,
    client: CtacClient,
    nestedRoute: @Composable (RouteBuilder.() -> Unit),
) {
    val session by rememberLoadingState { client.currentSession().map { CtacResult.Success(it) } }
    errorOr(session) {
        if (it == null) {
            route(route) {
                Router.current.navigate("${Routes.LOGIN.path}?${redirectQueryParameter}=${redirectTo}")
            }
        } else {
            route(route, nestedRoute = nestedRoute)
        }
    }
}

@Composable
fun LoginPage(client: CtacClient) {
    val session by rememberLoadingState { client.currentSession().map { CtacResult.Success(it) } }

    H1 { Text("Se connecter") }
    errorOr(session) { s ->
        if (s == null) {
            LoginForm(client)
        } else {
            val redirection = window.location.redirection()
            if (redirection !== null) {
                Router.current.navigate(redirection)
            } else {
                P {
                    Text("Déjà connecté en tant que ${s.username}.")
                }
            }
        }
    }
}

@Composable
fun LoginLink(client: CtacClient) {
    val scope = rememberCoroutineScope()
    val session by rememberLoadingState { client.currentSession().map { CtacResult.Success(it) } }

    errorOr(session) { s ->
        if (s == null) {
            NavLink(attrs = { classes(NavbarStylesheet.navbarLink) }, to = Routes.LOGIN.path) { Text("Se connecter") }
        } else {
            Span(attrs = {
                classes(NavbarStylesheet.navbarLink)
                style {
                    cursor("pointer")
                }
                onClick {
                    scope.launch {
                        client.logout()
                    }
                }
            }) {
                Text("Se déconnecter (${s.username})")
            }
        }
    }
}

private fun Location.redirection(): String? {
    return URL(this.href).searchParams.get(redirectQueryParameter)
}

@Composable
private fun LoginForm(client: CtacClient) {
    val scope = rememberCoroutineScope()
    var usernameInputState by remember { mutableStateOf("") }
    var passwordInputState by remember { mutableStateOf("") }
    var error: String? by remember { mutableStateOf(null) }

    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
        }
    }) {
        Form(action = null, attrs = {
            onSubmit {
                it.preventDefault()
                scope.launch {
                    error = try {
                        client.login(usernameInputState, passwordInputState)
                        null
                    } catch (e: Exception) {
                        ensureActive()
                        "Combinaison nom d'utilisateur/mot de passe invalide, veuillez réessayer"
                    }
                }
            }
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                justifyContent(JustifyContent.SpaceBetween)
                width(200.px)
                height(125.px)
            }
        }) {
            Label(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                }
            }) {
                Text("Nom d'utilisateur")
                TextInput {
                    autoComplete(AutoComplete.username)
                    required()
                    value(usernameInputState)
                    onInput { usernameInputState = it.value }
                }
            }
            Label(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                }
            }) {
                Text("Mot de passe")
                PasswordInput {
                    autoComplete(AutoComplete.currentPassword)
                    required()
                    value(passwordInputState)
                    onInput { passwordInputState = it.value }
                }
            }
            Input(InputType.Submit) {
                value("Se connecter")
            }
        }

        error?.let {
            ErrorMessage(it)
        }
    }
}
