package fr.sdis64.ui.mail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.client.CtacClient
import fr.sdis64.client.unseenMailSubjects
import fr.sdis64.ui.utilities.*
import org.jetbrains.compose.web.ExperimentalComposeWebSvgApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.svg.*

@Composable
fun UnseenMails(client: CtacClient) {
    val unseenMails by rememberLoadingState { client.unseenMailSubjects() }

    Tile({
        Span { Text("Mail CODIS") }
        errorOr(unseenMails) { mails ->
            EmailUnreadNotificationIcon(mails.isNotEmpty())
        }
    }) {
        loaderOr(unseenMails) { mails ->
            Ul(attrs = {
                style {
                    paddingLeft(16.px)
                    paddingRight(16.px)
                    marginTop(0.px)
                    listStyleType("none")
                }
            }) {
                if (mails.isNotEmpty()) {
                    mails.forEach {
                        Li(attrs = {
                            style {
                                property("white-space", "nowrap")
                                overflow("hidden")
                                property("text-overflow", "ellipsis")
                                marginBottom(5.px)
                            }
                        }) {
                            Text(it)
                        }
                    }
                } else {
                    Li {
                        Text("Pas d'e-mail non-lu")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeWebSvgApi::class)
@Composable
private fun EmailUnreadNotificationIcon(unreadMail: Boolean) {
    Div(attrs = {
        style {
            width(30.px)
            height(30.px)
            display(DisplayStyle.InlineBlock)
            position(Position.Relative)
            marginLeft(10.px)
        }
    }) {
        Svg(attrs = {
            height(35.px)
            width(35.px)
            viewBox("0 96 960 960")
            fill(Color.white.toString())
            style {
                position(Position.Absolute)
            }
        }) {
            Path(
                d = if (unreadMail) {
                    mailSvgDPath
                } else {
                    draftsSvgDPath
                }
            )
        }
        if (unreadMail) {
            Div(attrs = {
                style {
                    width(20.px)
                    height(20.px)
                    backgroundColor(Color.red)
                    borderRadius(30.px)
                    position(Position.Absolute)
                    top((-2).px)
                    right((-12).px)
                }
            })
        }
    }
}
