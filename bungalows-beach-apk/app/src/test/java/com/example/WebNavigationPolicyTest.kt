package com.example

import android.net.Uri
import com.example.web.WebAppViewModel
import com.example.web.WebErrorType
import com.example.web.WebNavigationPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebNavigationPolicyTest {

  @Test
  fun `site urls stay inside webview`() {
    val homeDecision = WebNavigationPolicy.evaluateUri(Uri.parse(WebNavigationPolicy.HOME_URL))
    assertEquals(WebNavigationPolicy.NavigationDecision.StayInWebView, homeDecision)

    val subpageDecision =
      WebNavigationPolicy.evaluateUri(
        Uri.parse("https://bungalowsbeachclub.github.io/bungalows_beach.ecu/#reservas")
      )
    assertEquals(WebNavigationPolicy.NavigationDecision.StayInWebView, subpageDecision)
  }

  @Test
  fun `whatsapp tel mailto and maps open externally`() {
    val whatsapp = WebNavigationPolicy.evaluateUri(Uri.parse("https://wa.me/593999999999"))
    assertTrue(whatsapp is WebNavigationPolicy.NavigationDecision.OpenExternalApp)

    val tel = WebNavigationPolicy.evaluateUri(Uri.parse("tel:+593999999999"))
    assertTrue(tel is WebNavigationPolicy.NavigationDecision.OpenExternalApp)

    val mail = WebNavigationPolicy.evaluateUri(Uri.parse("mailto:info@bungalowsbeach.ec"))
    assertTrue(mail is WebNavigationPolicy.NavigationDecision.OpenExternalApp)

    val maps = WebNavigationPolicy.evaluateUri(Uri.parse("https://maps.app.goo.gl/abcdef"))
    assertTrue(maps is WebNavigationPolicy.NavigationDecision.OpenExternalApp)
  }

  @Test
  fun `viewmodel tracks loading progress and error recovery`() {
    val vm = WebAppViewModel()
    assertTrue(vm.uiState.value.isLoading)
    assertFalse(vm.uiState.value.hasInitialPageLoaded)

    vm.onMainFrameError(isNetworkAvailable = false)
    assertEquals(WebErrorType.OFFLINE, vm.uiState.value.errorType)
    assertFalse(vm.uiState.value.isLoading)

    vm.onRetryRequested(isNetworkAvailable = true)
    assertNull(vm.uiState.value.errorType)
    assertTrue(vm.uiState.value.isLoading)

    vm.onPageFinished(
      url = WebNavigationPolicy.HOME_URL,
      title = "Bungalows Beach Club",
      canGoBack = false,
      canGoForward = false,
    )
    assertFalse(vm.uiState.value.isLoading)
    assertTrue(vm.uiState.value.hasInitialPageLoaded)
    assertEquals("Bungalows Beach Club", vm.uiState.value.pageTitle)
  }
}
