package com.example.web

import android.net.Uri

object WebNavigationPolicy {
  const val HOME_URL = "https://bungalowsbeachclub.github.io/bungalows_beach.ecu/"
  const val SITE_HOST = "bungalowsbeachclub.github.io"

  private val externalSchemes =
    setOf("tel", "mailto", "sms", "geo", "whatsapp", "tg", "viber", "intent")

  private val externalWebHosts =
    setOf(
      "wa.me",
      "api.whatsapp.com",
      "chat.whatsapp.com",
      "maps.google.com",
      "maps.app.goo.gl",
      "goo.gl",
      "instagram.com",
      "www.instagram.com",
      "facebook.com",
      "www.facebook.com",
      "m.facebook.com",
      "fb.me",
      "tiktok.com",
      "www.tiktok.com",
      "vm.tiktok.com",
      "youtube.com",
      "www.youtube.com",
      "youtu.be",
      "x.com",
      "twitter.com",
    )

  sealed interface NavigationDecision {
    data object StayInWebView : NavigationDecision

    data class OpenExternalApp(val uri: Uri) : NavigationDecision
  }

  fun evaluateUri(uri: Uri?): NavigationDecision {
    if (uri == null) return NavigationDecision.StayInWebView
    val scheme = uri.scheme?.lowercase().orEmpty()

    if (scheme in setOf("about", "data", "blob", "javascript")) {
      return NavigationDecision.StayInWebView
    }

    if (scheme in externalSchemes) {
      return NavigationDecision.OpenExternalApp(uri)
    }

    if (scheme == "http" || scheme == "https") {
      val host = uri.host?.lowercase().orEmpty()
      if (host in externalWebHosts) {
        return NavigationDecision.OpenExternalApp(uri)
      }
      return NavigationDecision.StayInWebView
    }

    return NavigationDecision.OpenExternalApp(uri)
  }
}
