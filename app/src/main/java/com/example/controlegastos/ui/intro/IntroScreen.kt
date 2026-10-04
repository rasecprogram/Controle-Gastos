package com.example.controlegastos.ui.intro

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
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
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.controlegastos.R
import android.view.LayoutInflater


@OptIn(UnstableApi::class)
@Composable
fun IntroScreen(
    onIntroFinalizada: () -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                val videoUri =
                    "android.resource://${context.packageName}/${R.raw.intro}"

                setMediaItem(
                    MediaItem.fromUri(videoUri)
                )

                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {
                if (playbackState == Player.STATE_ENDED) {
                    onIntroFinalizada()
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                LayoutInflater
                    .from(context)
                    .inflate(
                        R.layout.player_intro,
                        null,
                        false
                    ) as PlayerView
            },
            update = { playerView ->
                playerView.player = exoPlayer
            }
        )
    }
}