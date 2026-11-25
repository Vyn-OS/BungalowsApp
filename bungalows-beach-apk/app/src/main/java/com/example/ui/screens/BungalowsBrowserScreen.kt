package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.CoastalSplashOverlay
import com.example.ui.components.OfflineOrErrorOverlay
import com.example.web.WebAppViewModel
import com.example.web.WebNavigationPolicy
import kotlinx.coroutines.launch

@Composable
fun BungalowsBrowserScreen(
  viewModel: WebAppViewModel = viewModel(),
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
  var customVideoView by remember { mutableStateOf<View?>(null) }
  var customVideoCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

  val unavailableAppMessage = stringResource(R.string.external_app_unavailable)

  val fileChooserLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      val callback = pendingFileCallback
      pendingFileCallback = null
      if (callback == null) return@rememberLauncherForActivityResult

      val uris =
        if (result.resultCode == Activity.RESULT_OK) {
          WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        } else {
          null
        }
      callback.onReceiveValue(uris)
    }

  fun showUnavailableFeedback() {
    coroutineScope.launch { snackbarHostState.showSnackbar(unavailableAppMessage) }
  }

  fun launchExternalUri(uri: Uri) {
    if (uri.scheme.equals("intent", ignoreCase = true)) {
      try {
        val parsedIntent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
        val fallbackUrl = parsedIntent.getStringExtra("browser_fallback_url")
        if (parsedIntent.resolveActivity(context.packageManager) != null) {
          parsedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          context.startActivity(parsedIntent)
        } else if (!fallbackUrl.isNullOrBlank()) {
          webViewRef?.loadUrl(fallbackUrl)
        } else {
          showUnavailableFeedback()
        }
      } catch (_: Exception) {
        showUnavailableFeedback()
      }
      return
    }

    try {
      val intent =
        Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
      context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
      showUnavailableFeedback()
    }
  }

  val exitVideoFullscreen: () -> Unit = {
    customVideoCallback?.onCustomViewHidden()
    customVideoView = null
    customVideoCallback = null
  }

  BackHandler(enabled = customVideoView != null || uiState.canGoBack) {
    if (customVideoView != null) {
      exitVideoFullscreen()
    } else {
      val webView = webViewRef
      if (webView != null && webView.canGoBack()) {
        webView.goBack()
      }
    }
  }

  DisposableEffect(lifecycleOwner, webViewRef) {
    val observer = LifecycleEventObserver { _, event ->
      val webView = webViewRef ?: return@LifecycleEventObserver
      when (event) {
        Lifecycle.Event.ON_RESUME -> webView.onResume()
        Lifecycle.Event.ON_PAUSE -> webView.onPause()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  DisposableEffect(Unit) {
    onDispose {
      pendingFileCallback?.onReceiveValue(null)
      pendingFileCallback = null
      webViewRef?.apply {
        stopLoading()
        destroy()
      }
      webViewRef = null
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      AndroidView(
        modifier = Modifier.fillMaxSize().testTag("bungalows_webview"),
        factory = { viewContext ->
          createConfiguredWebView(
            context = viewContext,
            onPageStarted = { url, webView ->
              viewModel.onPageStarted(url, webView.canGoBack(), webView.canGoForward())
            },
            onPageCommitVisible = { webView ->
              viewModel.onPageCommitVisible(webView.canGoBack(), webView.canGoForward())
            },
            onPageFinished = { url, title, webView ->
              viewModel.onPageFinished(url, title, webView.canGoBack(), webView.canGoForward())
            },
            onHistoryUpdated = { webView ->
              viewModel.onNavigationStateChanged(
                canGoBack = webView.canGoBack(),
                canGoForward = webView.canGoForward(),
                url = webView.url,
              )
            },
            onProgressChanged = { progress, webView ->
              viewModel.onProgressChanged(progress, webView.canGoBack(), webView.canGoForward())
            },
            onMainFrameError = {
              viewModel.onMainFrameError(isNetworkAvailable(viewContext))
            },
            onOpenExternal = { uri -> launchExternalUri(uri) },
            onShowFileChooser = { callback, intent ->
              pendingFileCallback?.onReceiveValue(null)
              pendingFileCallback = callback
              try {
                fileChooserLauncher.launch(intent)
                true
              } catch (_: ActivityNotFoundException) {
                pendingFileCallback = null
                false
              }
            },
            onShowCustomVideoView = { view, callback ->
              customVideoView = view
              customVideoCallback = callback
            },
            onHideCustomVideoView = {
              customVideoView = null
              customVideoCallback = null
            },
          ).also { created ->
            webViewRef = created
            if (isNetworkAvailable(viewContext)) {
              created.loadUrl(WebNavigationPolicy.HOME_URL)
            } else {
              viewModel.onMainFrameError(isNetworkAvailable = false)
            }
          }
        },
        update = { /* State updates handled via callbacks and actions */ },
      )

      AnimatedVisibility(
        visible = uiState.isLoading && uiState.hasInitialPageLoaded && uiState.errorType == null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter),
      ) {
        LinearProgressIndicator(
          progress = { (uiState.progress.coerceIn(5, 100)) / 100f },
          modifier = Modifier.fillMaxWidth().height(3.dp).testTag("page_loading_indicator"),
          color = MaterialTheme.colorScheme.secondary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          strokeCap = StrokeCap.Round,
        )
      }

      OfflineOrErrorOverlay(
        errorType = uiState.errorType,
        onRetry = {
          val online = isNetworkAvailable(context)
          viewModel.onRetryRequested(online)
          if (online) {
            val webView = webViewRef
            if (webView != null) {
              val target =
                uiState.currentUrl.takeIf { it.startsWith("http") } ?: WebNavigationPolicy.HOME_URL
              webView.loadUrl(target)
            }
          }
        },
        modifier = Modifier.fillMaxSize(),
      )

      if (customVideoView != null) {
        AndroidView(
          factory = { ctx ->
            FrameLayout(ctx).apply {
              layoutParams =
                ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT,
                )
              setBackgroundColor(android.graphics.Color.BLACK)
              customVideoView?.let { video ->
                (video.parent as? ViewGroup)?.removeView(video)
                addView(video)
              }
            }
          },
          modifier = Modifier.fillMaxSize(),
        )
      }

      CoastalSplashOverlay(
        visible = !uiState.hasInitialPageLoaded && uiState.errorType == null,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createConfiguredWebView(
  context: Context,
  onPageStarted: (String?, WebView) -> Unit,
  onPageCommitVisible: (WebView) -> Unit,
  onPageFinished: (String?, String?, WebView) -> Unit,
  onHistoryUpdated: (WebView) -> Unit,
  onProgressChanged: (Int, WebView) -> Unit,
  onMainFrameError: () -> Unit,
  onOpenExternal: (Uri) -> Unit,
  onShowFileChooser: (ValueCallback<Array<Uri>>, Intent) -> Boolean,
  onShowCustomVideoView: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
  onHideCustomVideoView: () -> Unit,
): WebView {
  return WebView(context).apply {
    layoutParams =
      ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
      )
    isVerticalScrollBarEnabled = true
    isHorizontalScrollBarEnabled = false
    overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

    settings.apply {
      javaScriptEnabled = true
      domStorageEnabled = true
      databaseEnabled = true
      cacheMode = WebSettings.LOAD_DEFAULT
      loadWithOverviewMode = true
      useWideViewPort = true
      builtInZoomControls = true
      displayZoomControls = false
      mediaPlaybackRequiresUserGesture = false
      allowFileAccess = false
      allowContentAccess = true
      mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
      setSupportMultipleWindows(false)
    }

    val webView = this
    CookieManager.getInstance().apply {
      setAcceptCookie(true)
      setAcceptThirdPartyCookies(webView, true)
    }

    setDownloadListener { url, _, _, _, _ ->
      if (!url.isNullOrBlank()) {
        onOpenExternal(Uri.parse(url))
      }
    }

    webViewClient =
      object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
          view: WebView?,
          request: WebResourceRequest?,
        ): Boolean {
          val uri = request?.url ?: return false
          return when (val decision = WebNavigationPolicy.evaluateUri(uri)) {
            WebNavigationPolicy.NavigationDecision.StayInWebView -> false
            is WebNavigationPolicy.NavigationDecision.OpenExternalApp -> {
              onOpenExternal(decision.uri)
              true
            }
          }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
          super.onPageStarted(view, url, favicon)
          if (view != null) onPageStarted(url, view)
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
          super.onPageCommitVisible(view, url)
          if (view != null) onPageCommitVisible(view)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
          super.onPageFinished(view, url)
          if (view != null) onPageFinished(url, view.title, view)
        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
          super.doUpdateVisitedHistory(view, url, isReload)
          if (view != null) onHistoryUpdated(view)
        }

        override fun onReceivedError(
          view: WebView?,
          request: WebResourceRequest?,
          error: WebResourceError?,
        ) {
          super.onReceivedError(view, request, error)
          if (request?.isForMainFrame == true) {
            onMainFrameError()
          }
        }
      }

    webChromeClient =
      object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
          super.onProgressChanged(view, newProgress)
          if (view != null) onProgressChanged(newProgress, view)
        }

        override fun onShowFileChooser(
          webView: WebView?,
          filePathCallback: ValueCallback<Array<Uri>>?,
          fileChooserParams: FileChooserParams?,
        ): Boolean {
          if (filePathCallback == null || fileChooserParams == null) return false
          return onShowFileChooser(filePathCallback, fileChooserParams.createIntent())
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
          super.onShowCustomView(view, callback)
          onShowCustomVideoView(view, callback)
        }

        override fun onHideCustomView() {
          super.onHideCustomView()
          onHideCustomVideoView()
        }
      }
  }
}

private fun isNetworkAvailable(context: Context): Boolean {
  val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
  val activeNetwork = connectivityManager.activeNetwork ?: return false
  val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
  return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
