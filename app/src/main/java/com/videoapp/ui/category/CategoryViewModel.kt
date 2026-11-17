package com.videoapp.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videoapp.data.model.Category
import com.videoapp.data.model.Video
import com.videoapp.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()
    
    private val _categoryVideos = MutableStateFlow<List<Video>>(emptyList())
    val categoryVideos = _categoryVideos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private var currentCategoryId = ""
    private var currentPage = 1
    private var isLastPage = false
    
    init {
        loadCategories()
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            videoRepository.getCategories().onSuccess { categories ->
                _categories.value = categories
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load categories"
            }.onCompletion {
                _isLoading.value = false
            }
        }
    }
    
    fun loadCategoryVideos(categoryId: String, forceRefresh: Boolean = false) {
        currentCategoryId = categoryId
        currentPage = 1
        isLastPage = false
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            videoRepository.getVideosByCategory(categoryId, 1, forceRefresh).onSuccess { videos ->
                _categoryVideos.value = videos
                isLastPage = videos.size < 20
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load videos"
            }.onCompletion {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMoreCategoryVideos() {
        if (isLastPage || _isLoading.value || currentCategoryId.isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            currentPage++
            
            videoRepository.getVideosByCategory(currentCategoryId, currentPage).onSuccess { videos ->
                if (videos.isEmpty()) {
                    isLastPage = true
                    currentPage--
                } else {
                    _categoryVideos.value = _categoryVideos.value + videos
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
}
