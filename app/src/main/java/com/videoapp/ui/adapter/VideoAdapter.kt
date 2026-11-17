package com.videoapp.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.videoapp.data.model.Video
import com.videoapp.databinding.ItemVideoBinding
import com.videoapp.ui.player.VideoPlayerRouterActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class VideoAdapter(
    private val onVideoClick: (Video) -> Unit = {}
) : ListAdapter<Video, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = getItem(position)
        holder.bind(video, onVideoClick)
    }
    
    class VideoViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: Video, onVideoClick: (Video) -> Unit) {
            binding.tvTitle.text = video.title
            binding.tvChannelName.text = video.channelName
            binding.tvViewCount.text = video.getFormattedViewCount()
            binding.tvDuration.text = video.getFormattedDuration()
            
            // Show source type indicator
            val sourceIcon = when (video.sourceType) {
                Video.SourceType.YOUTUBE -> "@drawable/ic_youtube"
                Video.SourceType.HLS, Video.SourceType.DASH -> "@drawable/ic_stream"
                else -> "@drawable/ic_play_circle"
            }
            // binding.ivSourceType.setImageResource(getResourceId(sourceIcon))
            
            // Load thumbnail - prefer YouTube thumbnail if available
            val thumbnailUrl = video.getBestThumbnailUrl()
            Glide.with(binding.root.context)
                .load(thumbnailUrl)
                .apply(RequestOptions()
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .error(R.drawable.placeholder_thumbnail)
                    .transforms(CenterCrop(), RoundedCorners(8))
                )
                .into(binding.ivThumbnail)
            
            Glide.with(binding.root.context)
                .load(video.channelAvatar)
                .apply(RequestOptions()
                    .placeholder(R.drawable.placeholder_avatar)
                    .error(R.drawable.placeholder_avatar)
                    .transform(CenterCrop(), RoundedCorners(50))
                )
                .into(binding.ivChannelAvatar)
            
            // Click listener - route to appropriate player
            binding.root.setOnClickListener {
                onVideoClick(video)
            }
        }
    }
}

class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {
    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }
}

// Extension function to navigate to player
fun Context.navigateToVideoPlayer(video: Video) {
    val intent = Intent(this, VideoPlayerRouterActivity::class.java).apply {
        putExtra("video", video)
    }
    startActivity(intent)
}

// Legacy support for video ID only
fun Context.navigateToVideoPlayer(
    videoId: String,
    title: String = "",
    sourceType: String = "DIRECT",
    youtubeId: String? = null,
    embedUrl: String? = null
) {
    val intent = Intent(this, VideoPlayerRouterActivity::class.java).apply {
        putExtra("video_id", videoId)
        putExtra("title", title)
        putExtra("source_type", sourceType)
        youtubeId?.let { putExtra("youtube_id", it) }
        embedUrl?.let { putExtra("embed_url", it) }
    }
    startActivity(intent)
}
