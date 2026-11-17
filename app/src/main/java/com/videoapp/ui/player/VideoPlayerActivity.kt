package com.videoapp.ui.player

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.videoapp.R
import com.videoapp.databinding.ActivityVideoPlayerBinding
import com.videoapp.ui.adapter.VideoAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVideoPlayerBinding
    private val viewModel: VideoPlayerViewModel by viewModels()
    private var exoPlayer: ExoPlayer? = null
    private lateinit var relatedAdapter: VideoAdapter
    private var videoId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        videoId = intent.getStringExtra("video_id") ?: ""
        
        setupPlayer()
        setupRecyclerView()
        observeViewModel()
        viewModel.loadVideo(videoId)
    }
    
    private fun setupPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer
        
        // Keep screen on during playback
        binding.playerView.keepScreenOn = true
    }
    
    private fun setupRecyclerView() {
        relatedAdapter = VideoAdapter { video ->
            // Navigate to new video
            videoId = video.id
            viewModel.loadVideo(videoId)
            // Update UI
        }
        
        binding.rvRelated.apply {
            layoutManager = LinearLayoutManager(this@VideoPlayerActivity)
            adapter = relatedAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.video.observe(this@VideoPlayerActivity) { video ->
                video?.let {
                    playVideo(it.videoUrl)
                    binding.tvVideoTitle.text = it.title
                    binding.tvChannelName.text = it.channelName
                    binding.tvViewCount.text = it.getFormattedViewCount()
                    binding.tvDescription.text = it.description
                }
            }
            
            viewModel.relatedVideos.observe(this@VideoPlayerActivity) { videos ->
                relatedAdapter.submitList(videos)
            }
            
            viewModel.isLoading.observe(this@VideoPlayerActivity) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }
            
            viewModel.error.observe(this@VideoPlayerActivity) { error ->
                error?.let {
                    // Show error
                }
            }
        }
    }
    
    private fun playVideo(videoUrl: String) {
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }
    
    override fun onStart() {
        super.onStart()
        exoPlayer?.playWhenReady = true
    }
    
    override fun onResume() {
        super.onResume()
        // Handle fullscreen
    }
    
    override fun onPause() {
        super.onPause()
        exoPlayer?.playWhenReady = false
    }
    
    override fun onStop() {
        super.onStop()
        exoPlayer?.playWhenReady = false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
    
    override fun onBackPressed() {
        if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } else {
            super.onBackPressed()
        }
    }
}
