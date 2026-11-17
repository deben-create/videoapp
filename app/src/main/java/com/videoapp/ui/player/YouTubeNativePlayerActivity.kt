package com.videoapp.ui.player

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.videoapp.R
import com.videoapp.databinding.ActivityYoutubeNativePlayerBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * YouTube Native Player using Pierfrancesco Soffritti's Android YouTube Player
 * This wraps YouTube IFrame Player API - shows YouTube ads (mandatory per YouTube ToS)
 * Equivalent to youtube_player_flutter in the original app
 * 
 * Library: https://github.com/PierfrancescoSoffritti/android-youtube-player
 */
@AndroidEntryPoint
class YouTubeNativePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYoutubeNativePlayerBinding
    private var youtubePlayer: YouTubePlayer? = null
    private var videoId: String = ""
    private var startTimeMillis: Long = 0
    private var isFullscreen = false
    private var playlistId: String? = null
    
    companion object {
        const val EXTRA_VIDEO_ID = "youtube_video_id"
        const val EXTRA_START_TIME = "start_time_millis"
        const val EXTRA_PLAYLIST_ID = "youtube_playlist_id"
        const val EXTRA_TITLE = "video_title"
        const val EXTRA_CHANNEL = "channel_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeNativePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: ""
        startTimeMillis = intent.getLongExtra(EXTRA_START_TIME, 0)
        playlistId = intent.getStringExtra(EXTRA_PLAYLIST_ID)
        val title = intent.getStringExtra(EXTRA_TITLE) ?? ""
        val channel = intent.getStringExtra(EXTRA_CHANNEL) ?? ""
        
        binding.tvVideoTitle.text = title
        binding.tvChannelName.text = channel
        
        // Initialize YouTube Player
        initializeYouTubePlayer()
        
        setupControls()
        
        // Lifecycle
        val youTubePlayerView = binding.youtubePlayerView
        val lifecycle = lifecycle
        lifecycle.addObserver(youTubePlayerView)
    }
    
    private fun initializeYouTubePlayer() {
        val youTubePlayerView = binding.youtubePlayerView
        
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youtubePlayer = youTubePlayer
                
                // Configure player
                youTubePlayer.setManageAudioFocus(true)
                
                // Load video or playlist
                if (playlistId != null && playlistId!.isNotBlank()) {
                    youTubePlayer.loadPlaylist(playlistId!!, 0, startTimeMillis.toInt())
                } else {
                    youTubePlayer.loadVideo(videoId, startTimeMillis.toInt())
                }
            }
            
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                when (state) {
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.BUFFERING -> {
                        binding.progressBar.isVisible = true
                    }
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> {
                        binding.progressBar.isVisible = false
                    }
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> {
                        binding.progressBar.isVisible = false
                    }
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED -> {
                        // Auto-play next in playlist if available
                        playNextInPlaylist()
                    }
                    else -> {}
                }
            }
            
            override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                binding.progressBar.isVisible = false
                Log.e("YouTubePlayer", "Error: $error")
                Toast.makeText(this@YouTubeNativePlayerActivity, "Playback error: $error", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun setupControls() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        
        binding.btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }
        
        binding.btnPlaylist.setOnClickListener {
            showPlaylistBottomSheet()
        }
        
        binding.btnShare.setOnClickListener {
            shareVideo()
        }
    }
    
    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        if (isFullscreen) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            binding.toolbar.isVisible = false
            binding.bottomControls.isVisible = false
            binding.youtubePlayerView.layoutParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            binding.toolbar.isVisible = true
            binding.bottomControls.isVisible = true
            binding.youtubePlayerView.layoutParams.height = resources.getDimensionPixelSize(R.dimen.youtube_player_height)
        }
    }
    
    private fun playNextInPlaylist() {
        youtubePlayer?.playNext()
    }
    
    private fun showPlaylistBottomSheet() {
        // TODO: Implement playlist/related videos bottom sheet
        // Similar to the original app's related videos
    }
    
    private fun shareVideo() {
        val shareUrl = "https://youtu.be/$videoId"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this video: $shareUrl")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }
    
    override fun onBackPressed() {
        if (isFullscreen) {
            toggleFullscreen()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onPause() {
        super.onPause()
        youtubePlayer?.pause()
    }
    
    override fun onResume() {
        super.onResume()
        // YouTube player handles resume automatically
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cleanup handled by lifecycle observer
    }
}
