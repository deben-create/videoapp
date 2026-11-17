package com.videoapp.ui.main

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
class MainViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _trendingVideos = MutableStateFlow<List<Video>>(emptyList())
    val trendingVideos = _trendingVideos.asStateFlow()
    
    private val _categories = MutableStateFlow<List<com.videoapp.data.model.Category>>(emptyList())
    val categories = _categories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private var currentPage = 1
    private var isLastPage = false
    
    init {
        loadInitialData()
    }
    
    fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            videoRepository.getTrendingVideos(1).onSuccess { videos ->
                _trendingVideos.value = videos
                currentPage = 1
                isLastPage = videos.size < 20
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load videos"
            }.onCompletion {
                _isLoading.value = false
            }
            
            videoRepository.getCategories().onSuccess { categories ->
                _categories.value = categories
            }
        }
    }
    
    fun loadMoreTrendingVideos() {
        if (isLastPage || _isLoading.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            currentPage++
            
            videoRepository.getTrendingVideos(currentPage).onSuccess { videos ->
                if (videos.isEmpty()) {
                    isLastPage = true
                    currentPage--
                } else {
                    _trendingVideos.value = _trendingVideos.value + videos
                    isLastPage = videos.size < 20
                }
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load more videos"
                currentPage--
            }.onCompletion {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        currentPage = 1
        isLastPage = false
        loadInitialData()
    }
}
