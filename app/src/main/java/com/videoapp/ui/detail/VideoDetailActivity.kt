package com.videoapp.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.videoapp.R
import com.videoapp.databinding.ActivityVideoDetailBinding
import com.videoapp.ui.adapter.navigateToVideoPlayer
import com.videoapp.ui.adapter.VideoAdapter
import com.videoapp.ui.player.VideoPlayerRouterActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VideoDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVideoDetailBinding
    private val viewModel: VideoDetailViewModel by viewModels()
    private lateinit var relatedAdapter: VideoAdapter
    private var videoId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        videoId = intent.getStringExtra("video_id") ?: ""
        
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        viewModel.loadVideo(videoId)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
    
    private fun setupRecyclerView() {
        relatedAdapter = VideoAdapter { video ->
            // Use the new router to navigate to appropriate player
            navigateToVideoPlayer(this, video)
        }
        
        binding.rvRelated.apply {
            layoutManager = LinearLayoutManager(this@VideoDetailActivity)
            adapter = relatedAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.video.observe(this@VideoDetailActivity) { video ->
                video?.let {
                    binding.tvVideoTitle.text = it.title
                    binding.tvChannelName.text = it.channelName
                    binding.tvViewCount.text = it.getFormattedViewCount()
                    binding.tvPublishedDate.text = it.publishedAt
                    binding.tvDescription.text = it.description
                    
                    Glide.with(this@VideoDetailActivity)
                        .load(it.getBestThumbnailUrl())
                        .placeholder(R.drawable.placeholder_thumbnail)
                        .error(R.drawable.placeholder_thumbnail)
                        .centerCrop()
                        .into(binding.ivThumbnail)
                    
                    Glide.with(this@VideoDetailActivity)
                        .load(it.channelAvatar)
                        .placeholder(R.drawable.placeholder_avatar)
                        .error(R.drawable.placeholder_avatar)
                        .circleCrop()
                        .into(binding.ivChannelAvatar)
                    
                    // Tags
                    binding.chipGroupTags.removeAllViews()
                    it.tags.forEach { tag ->
                        val chip = com.google.android.material.chip.Chip(this@VideoDetailActivity).apply {
                            text = tag
                            isCloseIconVisible = false
                            isClickable = false
                        }
                        binding.chipGroupTags.addView(chip)
                    }
                }
            }
            
            viewModel.relatedVideos.observe(this@VideoDetailActivity) { videos ->
                relatedAdapter.submitList(videos)
            }
            
            viewModel.isLoading.observe(this@VideoDetailActivity) { isLoading ->
                binding.progressBar.isVisible = isLoading
                binding.scrollView.isVisible = !isLoading
            }
            
            viewModel.error.observe(this@VideoDetailActivity) { error ->
                error?.let {
                    // Show error
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
