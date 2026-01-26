package com.jotadev.mediflow.screens.recursos

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun VisorRecursoScreen(
    url: String,
    tipo: String,
    titulo: String,
    onBack: () -> Unit
) {
    // Decodificar URL por si viene encodeada
    // Pero asumimos que navegación nos pasa string limpio o nosotros lo manejamos.
    // Si usas navegación con argumentos en URL, asegúrate de decodificar antes de llamar a este composable
    // o pasarla decodificada.

    val isVideo = tipo.contains("video", ignoreCase = true) || url.endsWith(".mp4", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isVideo) {
            VideoPlayer(url = url)
        } else {
            val isDoc = tipo.contains("pdf", ignoreCase = true) ||
                    tipo.contains("word", ignoreCase = true) ||
                    url.endsWith(".pdf", ignoreCase = true) ||
                    url.endsWith(".doc", ignoreCase = true) ||
                    url.endsWith(".docx", ignoreCase = true)

            val finalUrl = if (isDoc) {
                "https://docs.google.com/gview?embedded=true&url=${URLEncoder.encode(url, StandardCharsets.UTF_8.toString())}"
            } else {
                url
            }

            WebViewer(url = finalUrl)
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewer(url: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                
                loadUrl(url)
            }
        },
        update = { webView ->
            // Evitar recargar si la URL no cambió
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
