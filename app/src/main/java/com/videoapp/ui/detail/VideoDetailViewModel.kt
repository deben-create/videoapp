package com.videoapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videoapp.data.model.Video
import com.videoapp.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoDetailViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _video = MutableStateFlow<Video?>(null)
    val video = _video.asStateFlow()
    
    private val _relatedVideos = MutableStateFlow<List<Video>>(emptyList())
    val relatedVideos = _relatedVideos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    fun loadVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            videoRepository.getVideoDetail(videoId).onSuccess { video ->
                _video.value = video
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load video"
            }.onCompletion {
                _isLoading.value = false
            }
            
            videoRepository.getRelatedVideos(videoId).onSuccess { videos ->
                _relatedVideos.value = videos
            }
        }
    }
}
