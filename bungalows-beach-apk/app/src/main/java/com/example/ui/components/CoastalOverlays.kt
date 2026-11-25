package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CoastalTeal
import com.example.ui.theme.SunsetCoral
import com.example.ui.theme.WarmSand
import com.example.web.WebErrorType

@Composable
fun CoastalSplashOverlay(
  visible: Boolean,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(animationSpec = tween(150)),
    exit = fadeOut(animationSpec = tween(420)),
    modifier = modifier,
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(CoastalTeal, Color(0xFF073B4E), Color(0xFF052A38))
            )
          )
          .padding(32.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.widthIn(max = 360.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White,
          tonalElevation = 8.dp,
          shadowElevation = 12.dp,
          modifier = Modifier.size(124.dp),
        ) {
          Image(
            painter = painterResource(id = R.drawable.ic_bungalows_logo),
            contentDescription = stringResource(R.string.cd_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape),
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.displaySmall,
          color = WarmSand,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = stringResource(R.string.brand_subtitle),
          style = MaterialTheme.typography.labelLarge,
          color = WarmSand.copy(alpha = 0.8f),
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        LinearProgressIndicator(
          modifier = Modifier.width(180.dp).height(4.dp).clip(CircleShape),
          color = SunsetCoral,
          trackColor = WarmSand.copy(alpha = 0.22f),
          strokeCap = StrokeCap.Round,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = stringResource(R.string.loading_experience),
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.78f),
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
fun OfflineOrErrorOverlay(
  errorType: WebErrorType?,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = errorType != null,
    enter = fadeIn(animationSpec = tween(250)),
    exit = fadeOut(animationSpec = tween(200)),
    modifier = modifier,
  ) {
    val isOffline = errorType == WebErrorType.OFFLINE
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .padding(horizontal = 24.dp, vertical = 32.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
      ) {
        Surface(
          shape = RoundedCornerShape(24.dp),
          tonalElevation = 2.dp,
          shadowElevation = 6.dp,
          modifier = Modifier.fillMaxWidth(0.85f),
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_offline_beach),
            contentDescription = stringResource(R.string.cd_offline_illustration),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(210.dp),
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
          text =
            stringResource(
              if (isOffline) R.string.offline_title else R.string.error_page_title
            ),
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text =
            stringResource(
              if (isOffline) R.string.offline_description else R.string.error_page_description
            ),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
          onClick = onRetry,
          shape = RoundedCornerShape(16.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
          modifier = Modifier.heightIn(min = 50.dp).widthIn(min = 180.dp).testTag("retry_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.action_retry),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}
