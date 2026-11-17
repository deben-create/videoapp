package com.videoapp.ui.search

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
class SearchViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _searchResults = MutableStateFlow<List<Video>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private var currentQuery = ""
    private var currentPage = 1
    private var isLastPage = false
    
    fun search(query: String) {
        currentQuery = query
        currentPage = 1
        isLastPage = false
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            videoRepository.searchVideos(query, 1).onSuccess { videos ->
                _searchResults.value = videos
                isLastPage = videos.size < 20
            }.onFailure { exception ->
                _error.value = exception.message ?: "Search failed"
            }.onCompletion {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMore() {
        if (isLastPage || _isLoading.value || currentQuery.isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            currentPage++
            
            videoRepository.searchVideos(currentQuery, currentPage).onSuccess { videos ->
                if (videos.isEmpty()) {
                    isLastPage = true
                    currentPage--
                } else {
                    _searchResults.value = _searchResults.value + videos
                    isLastPage = videos.size < 20
                }
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load more results"
                currentPage--
            }.onCompletion {
                _isLoading.value = false
            }
        }
    }
}
