package com.videoapp.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.MediaSessionCompat
import android.media.session.PlaybackStateCompat
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.MediaButtonReceiver
import com.videoapp.R
import com.videoapp.ui.player.VideoPlayerActivity

class MediaPlaybackService : Service() {

    private val binder = LocalBinder()
    private var mediaSession: MediaSessionCompat? = null
    private var playbackState: PlaybackStateCompat.Builder? = null
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "media_playback_channel"

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        initMediaSession()
        createNotificationChannel()
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "VideoAppMediaSession").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setMediaButtonReceiver(MediaButtonReceiver.buildMediaButtonPendingIntent(this))
            
            playbackState = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_FAST_FORWARD or
                    PlaybackStateCompat.ACTION_REWIND
                )
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1f)
            setPlaybackState(playbackState?.build())
        }
    }

    private fun createNotificationChannel() {
        // Create notification channel for Android O+
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, VideoPlayerActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VideoApp")
            .setContentText("Playing video...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()
    }

    fun updatePlaybackState(state: Int, position: Long, speed: Float) {
        playbackState?.setState(state, position, speed)
        mediaSession?.setPlaybackState(playbackState?.build())
    }

    fun updateMetadata(title: String, artist: String, artworkUri: String?) {
        // Update media metadata
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        mediaSession = null
        stopForeground(true)
    }
}
