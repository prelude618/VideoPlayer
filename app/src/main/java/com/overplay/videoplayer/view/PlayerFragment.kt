package com.overplay.videoplayer.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.overplay.videoplayer.R
import com.overplay.videoplayer.databinding.ActivityPlayerBinding
import com.overplay.videoplayer.viewmodel.PlayerViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import androidx.lifecycle.Observer

class PlayerFragment : Fragment() {
    private val playerViewModel: PlayerViewModel by viewModel()
    private var player: SimpleExoPlayer? = null
    private var currentWindow = 0
    private var playbackPosition = 0L

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return viewBinding.root
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
            playMedia()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
            playMedia()
        }
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(getString(R.string.overplay_mp4))
                exoPlayer.setMediaItem(mediaItem)
            }
    }

    private fun playMedia() {
        player?.run {
            playWhenReady = true
            seekTo(currentWindow, playbackPosition)
            prepare()
        }

        playerViewModel.playMedia().apply {
            observe(viewLifecycleOwner, Observer {
                player?.run {
                    playWhenReady = false
                    stop()
                    seekTo(0)
                }
            })
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        viewBinding.videoView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            release()
        }
        player = null
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }
}