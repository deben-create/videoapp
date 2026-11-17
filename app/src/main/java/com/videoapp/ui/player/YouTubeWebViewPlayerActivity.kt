package com.videoapp.ui.player

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.videoapp.R
import com.videoapp.databinding.ActivityYoutubeWebviewPlayerBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * YouTube Embed Player using WebView
 * Equivalent to flutter_inappwebview in the original app
 * Loads youtube.com/embed/VIDEO_ID - still shows YouTube ads
 */
@AndroidEntryPoint
class YouTubeWebViewPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYoutubeWebviewPlayerBinding
    private var videoId: String = ""
    private var playlistId: String? = null
    private var startTimeSeconds: Int = 0
    private var title: String = ""
    private var channel: String = ""
    private var customEmbedUrl: String? = null
    private var customView: View? = null
    
    companion object {
        const val EXTRA_VIDEO_ID = "youtube_video_id"
        const val EXTRA_PLAYLIST_ID = "youtube_playlist_id"
        const val EXTRA_START_TIME = "start_time_seconds"
        const val EXTRA_TITLE = "video_title"
        const val EXTRA_CHANNEL = "channel_name"
        const val EXTRA_EMBED_URL = "custom_embed_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeWebviewPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: ""
        playlistId = intent.getStringExtra(EXTRA_PLAYLIST_ID)
        startTimeSeconds = intent.getIntExtra(EXTRA_START_TIME, 0)
        title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        channel = intent.getStringExtra(EXTRA_CHANNEL) ?: ""
        customEmbedUrl = intent.getStringExtra(EXTRA_EMBED_URL)
        
        binding.tvVideoTitle.text = title
        binding.tvChannelName.text = channel
        
        setupWebView()
        loadVideo()
        
        setupControls()
    }
    
    private fun setupWebView() {
        val webView = binding.webView
        val settings = webView.settings
        
        // Enable JavaScript and media playback
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.setSupportFullScreen(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setPluginState(WebSettings.PluginState.ON)
        
        // Allow mixed content (for HTTP embed URLs)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        
        // Hardware acceleration for video
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        // Custom WebChromeClient for fullscreen handling
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                super.onShowCustomView(view, callback)
                if (customView != null) {
                    callback.onCustomViewHidden()
                    return
                }
                customView = view
                binding.fullscreenContainer.addView(view)
                binding.fullscreenContainer.visibility = View.VISIBLE
                binding.webView.visibility = View.GONE
                binding.toolbar.visibility = View.GONE
                binding.bottomControls.visibility = View.GONE
                requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            
            override fun onHideCustomView() {
                super.onHideCustomView()
                if (customView != null) {
                    binding.fullscreenContainer.removeView(customView)
                    customView = null
                }
                binding.fullscreenContainer.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
                binding.toolbar.visibility = View.VISIBLE
                binding.bottomControls.visibility = View.VISIBLE
                requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                binding.progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                // Keep navigation within WebView for YouTube links
                if (url.contains("youtube.com") || url.contains("youtu.be") || url.contains("googlevideo.com")) {
                    return false
                }
                // Open external links in browser
                if (url.startsWith("http")) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    startActivity(intent)
                    return true
                }
                return false
            }
            
            override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                super.onReceivedError(view, request, error)
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun loadVideo() {
        val embedUrl = buildEmbedUrl()
        binding.webView.loadUrl(embedUrl)
    }
    
    private fun buildEmbedUrl(): String {
        // Use custom embed URL if provided (for non-YouTube embeds)
        if (customEmbedUrl != null && customEmbedUrl!.isNotBlank()) {
            return customEmbedUrl!
        }
        
        // Build YouTube embed URL
        return "https://www.youtube.com/embed/$videoId?${buildQueryParams()}"
    }
    
    private fun buildQueryParams(): String {
        val params = mutableListOf<String>()
        
        // Autoplay (may be blocked by browser policy)
        params.add("autoplay=1")
        
        // Start time
        if (startTimeSeconds > 0) {
            params.add("start=$startTimeSeconds")
        }
        
        // Playlist
        playlistId?.let {
            params.add("list=$it")
        }
        
        // Player controls
        params.add("controls=1")
        params.add("showinfo=0")
        params.add("rel=0")  // Don't show related videos at end
        params.add("modestbranding=1")
        params.add("playsinline=1")  // Important for mobile
        params.add("enablejsapi=1")  // Enable JS API for control
        params.add("origin=https://www.youtube.com")
        
        // CC/Language
        params.add("cc_load_policy=1")
        
        return params.joinToString("&")
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
        
        binding.btnRefresh.setOnClickListener {
            binding.webView.reload()
        }
    }
    
    private fun toggleFullscreen() {
        if (binding.fullscreenContainer.visibility == View.VISIBLE) {
            // Already in custom fullscreen, exit
            (binding.webView.webChromeClient as? WebChromeClient)?.onHideCustomView()
        } else {
            // Request fullscreen via JS
            binding.webView.evaluateJavascript(
                "document.querySelector('video')?.webkitEnterFullscreen?.()"
            ) { _ -> }
        }
    }
    
    private fun showPlaylistBottomSheet() {
        // TODO: Implement playlist bottom sheet
        // Could load playlist in a separate WebView or native RecyclerView
    }
    
    private fun shareVideo() {
        val shareUrl = "https://youtu.be/$videoId"
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, "Watch this video: $shareUrl")
        }
        startActivity(android.content.Intent.createChooser(intent, "Share via"))
    }
    
    override fun onBackPressed() {
        if (binding.webView.canGoBack() && !videoId.isNullOrEmpty()) {
            // If user navigated within YouTube, go back
            binding.webView.goBack()
        } else if (customView != null) {
            // Exit fullscreen
            (binding.webView.webChromeClient as? WebChromeClient)?.onHideCustomView()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
        binding.webView.evaluateJavascript("document.querySelector('video')?.pause?.()") { _ -> }
    }
    
    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        binding.webView.loadUrl("about:blank")
        binding.webView.stopLoading()
        binding.webView.webViewClient = null
        binding.webView.webChromeClient = null
        binding.webView.destroy()
    }
}
