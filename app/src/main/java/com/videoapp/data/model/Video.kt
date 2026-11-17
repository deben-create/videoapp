package com.videoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "videos")
data class Video(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String,
    
    // Support multiple source types
    @SerializedName("videoUrl")
    val videoUrl: String? = null,           // Direct MP4/HLS/DASH URL
    
    @SerializedName("youtubeId")
    val youtubeId: String? = null,          // YouTube video ID (e.g., "dQw4w9WgXcQ")
    
    @SerializedName("youtubePlaylistId")
    val youtubePlaylistId: String? = null,  // For playlists/series
    
    @SerializedName("embedUrl")
    val embedUrl: String? = null,           // Custom embed URL (iframe src)
    
    @SerializedName("sourceType")
    val sourceType: SourceType = SourceType.DIRECT,  // DIRECT, YOUTUBE, EMBED, HLS
    
    @SerializedName("duration")
    val duration: Long = 0,
    
    @SerializedName("category")
    val category: String = "",
    
    @SerializedName("channelName")
    val channelName: String = "",
    
    @SerializedName("channelAvatar")
    val channelAvatar: String = "",
    
    @SerializedName("viewCount")
    val viewCount: Long = 0,
    
    @SerializedName("likeCount")
    val likeCount: Long = 0,
    
    @SerializedName("publishedAt")
    val publishedAt: String = "",
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("quality")
    val quality: List<String> = listOf("480p", "720p", "1080p"),
    
    // Episode info for series
    @SerializedName("seasonNumber")
    val seasonNumber: Int = 1,
    
    @SerializedName("episodeNumber")
    val episodeNumber: Int = 1,
    
    @SerializedName("seriesId")
    val seriesId: String? = null
) {
    enum class SourceType {
        DIRECT,      // Direct MP4/HLS/DASH URL (ExoPlayer)
        YOUTUBE,     // YouTube video ID (YouTube Player API)
        EMBED,       // Custom embed URL (WebView)
        HLS,         // HLS stream (ExoPlayer)
        DASH         // DASH stream (ExoPlayer)
    }
    
    // Helper to determine which player to use
    val playerType: PlayerType
        get() = when (sourceType) {
            SourceType.YOUTUBE -> PlayerType.YOUTUBE_NATIVE
            SourceType.EMBED -> PlayerType.WEBVIEW
            SourceType.HLS, SourceType.DASH, SourceType.DIRECT -> PlayerType.EXOPLAYER
        }
    
    enum class PlayerType {
        EXOPLAYER,           // Native ExoPlayer
        YOUTUBE_NATIVE,      // YouTube Player API (shows ads)
        YOUTUBE_WEBVIEW,     // YouTube embed in WebView (shows ads)
        WEBVIEW              // Generic WebView embed
    }
    
    fun getFormattedDuration(): String {
        if (duration <= 0) return ""
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    fun getFormattedViewCount(): String {
        return when {
            viewCount >= 1000000 -> "${viewCount / 1000000}M views"
            viewCount >= 1000 -> "${viewCount / 1000}K views"
            else -> "$viewCount views"
        }
    }
    
    // Get best available thumbnail
    fun getBestThumbnailUrl(): String {
        return if (youtubeId != null && youtubeId!.isNotBlank()) {
            "https://img.youtube.com/vi/$youtubeId/maxresdefault.jpg"
        } else {
            thumbnailUrl
        }
    }
}

@Serializable
data class VideoResponse(
    val videos: List<Video>,
    val nextPageToken: String?,
    val totalResults: Int
)

@Serializable
data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("youtubePlaylistId") val youtubePlaylistId: String? = null
)

// For series/dramas with multiple episodes
@Serializable
data class VideoSeries(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String,
    @SerializedName("totalEpisodes") val totalEpisodes: Int,
    @SerializedName("seasonCount") val seasonCount: Int,
    @SerializedName("category") val category: String,
    @SerializedName("youtubePlaylistId") val youtubePlaylistId: String? = null
)
