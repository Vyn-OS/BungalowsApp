package com.example.web

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class WebErrorType {
  OFFLINE,
  LOAD_FAILED,
}

data class WebUiState(
  val currentUrl: String = WebNavigationPolicy.HOME_URL,
  val pageTitle: String = "",
  val progress: Int = 0,
  val isLoading: Boolean = true,
  val hasInitialPageLoaded: Boolean = false,
  val canGoBack: Boolean = false,
  val canGoForward: Boolean = false,
  val errorType: WebErrorType? = null,
)

class WebAppViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(WebUiState())
  val uiState: StateFlow<WebUiState> = _uiState.asStateFlow()

  fun onPageStarted(url: String?, canGoBack: Boolean, canGoForward: Boolean) {
    _uiState.update { state ->
      state.copy(
        currentUrl = url ?: state.currentUrl,
        isLoading = true,
        progress = if (state.progress < 15) 15 else state.progress,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        errorType = null,
      )
    }
  }

  fun onPageCommitVisible(canGoBack: Boolean, canGoForward: Boolean) {
    _uiState.update { state ->
      state.copy(
        hasInitialPageLoaded = true,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
      )
    }
  }

  fun onPageFinished(url: String?, title: String?, canGoBack: Boolean, canGoForward: Boolean) {
    _uiState.update { state ->
      state.copy(
        currentUrl = url ?: state.currentUrl,
        pageTitle = title.orEmpty(),
        isLoading = false,
        progress = 100,
        hasInitialPageLoaded = true,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
      )
    }
  }

  fun onProgressChanged(newProgress: Int, canGoBack: Boolean, canGoForward: Boolean) {
    _uiState.update { state ->
      val shouldReveal = state.hasInitialPageLoaded || (newProgress >= 85 && state.errorType == null)
      state.copy(
        progress = newProgress,
        isLoading = newProgress < 100,
        hasInitialPageLoaded = shouldReveal,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
      )
    }
  }

  fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean, url: String?) {
    _uiState.update { state ->
      state.copy(
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        currentUrl = url ?: state.currentUrl,
      )
    }
  }

  fun onMainFrameError(isNetworkAvailable: Boolean) {
    _uiState.update { state ->
      state.copy(
        isLoading = false,
        hasInitialPageLoaded = true,
        errorType = if (isNetworkAvailable) WebErrorType.LOAD_FAILED else WebErrorType.OFFLINE,
      )
    }
  }

  fun onRetryRequested(isNetworkAvailable: Boolean) {
    if (!isNetworkAvailable) {
      _uiState.update { state ->
        state.copy(
          isLoading = false,
          hasInitialPageLoaded = true,
          errorType = WebErrorType.OFFLINE,
        )
      }
    } else {
      _uiState.update { state ->
        state.copy(
          errorType = null,
          isLoading = true,
          progress = 15,
        )
      }
    }
  }
}
