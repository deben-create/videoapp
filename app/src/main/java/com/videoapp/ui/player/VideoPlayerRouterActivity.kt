package com.videoapp.ui.player

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.videoapp.data.model.Video
import dagger.hilt.android.AndroidEntryPoint

/**
 * Router activity that launches the appropriate player based on video source type
 * This is the entry point from VideoDetailActivity, MainActivity, etc.
 */
@AndroidEntryPoint
class VideoPlayerRouterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No UI - immediately route to appropriate player
        
        val video = intent.getParcelableExtra<Video>("video")
        val videoId = intent.getStringExtra("video_id")
        val youtubeId = intent.getStringExtra("youtube_id")
        val sourceType = intent.getStringExtra("source_type")
        val embedUrl = intent.getStringExtra("embed_url")
        
        if (video != null) {
            routeToPlayer(video)
        } else if (videoId != null) {
            // Legacy support - create minimal video object
            val minimalVideo = Video(
                id = videoId,
                title = intent.getStringExtra("title") ?: "",
                description = "",
                thumbnailUrl = "",
                videoUrl = videoId, // Could be direct URL or YouTube ID
                sourceType = Video.SourceType.valueOf(sourceType ?: "DIRECT")
            )
            routeToPlayer(minimalVideo)
        } else if (youtubeId != null) {
            launchYouTubeNativePlayer(youtubeId)
        } else if (embedUrl != null) {
            launchWebViewPlayer(embedUrl, intent.getStringExtra("title") ?: "")
        } else {
            Log.e("VideoPlayerRouter", "No video data provided")
            finish()
        }
    }
    
    private fun routeToPlayer(video: Video) {
        when (video.playerType) {
            Video.PlayerType.YOUTUBE_NATIVE -> {
                if (video.youtubeId != null) {
                    launchYouTubeNativePlayer(video.youtubeId!!, video)
                } else {
                    // Fallback to WebView if no YouTube ID but source type is YouTube
                    launchWebViewPlayer("https://www.youtube.com/embed/${video.videoUrl}", video.title)
                }
            }
            
            Video.PlayerType.YOUTUBE_WEBVIEW -> {
                // Use WebView for YouTube embed (alternative to native player)
                val embedUrl = video.embedUrl ?: "https://www.youtube.com/embed/${video.youtubeId}"
                launchWebViewPlayer(embedUrl, video.title, video)
            }
            
            Video.PlayerType.WEBVIEW -> {
                // Generic WebView embed (custom embed URL)
                val embedUrl = video.embedUrl ?: video.videoUrl ?: ""
                if (embedUrl.isNotBlank()) {
                    launchWebViewPlayer(embedUrl, video.title, video)
                } else {
                    // Fallback to ExoPlayer
                    launchExoPlayer(video)
                }
            }
            
            Video.PlayerType.EXOPLAYER -> {
                launchExoPlayer(video)
            }
        }
    }
    
    private fun launchYouTubeNativePlayer(youtubeId: String, video: Video? = null) {
        val intent = Intent(this, YouTubeNativePlayerActivity::class.java).apply {
            putExtra(YouTubeNativePlayerActivity.EXTRA_VIDEO_ID, youtubeId)
            video?.let {
                putExtra(YouTubeNativePlayerActivity.EXTRA_TITLE, it.title)
                putExtra(YouTubeNativePlayerActivity.EXTRA_CHANNEL, it.channelName)
                putExtra(YouTubeNativePlayerActivity.EXTRA_START_TIME, 0L)
                it.youtubePlaylistId?.let { putExtra(YouTubeNativePlayerActivity.EXTRA_PLAYLIST_ID, it) }
            }
        }
        startActivity(intent)
        finish()
    }
    
    private fun launchWebViewPlayer(embedUrl: String, title: String, video: Video? = null) {
        val intent = Intent(this, YouTubeWebViewPlayerActivity::class.java).apply {
            putExtra(YouTubeWebViewPlayerActivity.EXTRA_EMBED_URL, embedUrl)
            putExtra(YouTubeWebViewPlayerActivity.EXTRA_TITLE, title)
            video?.let {
                putExtra(YouTubeWebViewPlayerActivity.EXTRA_VIDEO_ID, it.youtubeId ?: it.id)
                putExtra(YouTubeWebViewPlayerActivity.EXTRA_CHANNEL, it.channelName)
                it.youtubePlaylistId?.let { putExtra(YouTubeWebViewPlayerActivity.EXTRA_PLAYLIST_ID, it) }
            }
        }
        startActivity(intent)
        finish()
    }
    
    private fun launchExoPlayer(video: Video) {
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            putExtra("video_id", video.id)
            putExtra("video_url", video.videoUrl)
            putExtra("video_title", video.title)
        }
        startActivity(intent)
        finish()
    }
}
