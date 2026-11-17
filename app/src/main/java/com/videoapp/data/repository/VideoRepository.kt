package com.videoapp.data.repository

import com.videoapp.data.local.VideoDao
import com.videoapp.data.model.Video
import com.videoapp.data.model.VideoResponse
import com.videoapp.data.network.VideoApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val apiService: VideoApiService,
    private val videoDao: VideoDao
) {
    
    suspend fun getTrendingVideos(page: Int = 1, forceRefresh: Boolean = false): Result<List<Video>> {
        return try {
            if (!forceRefresh) {
                // Return cached data first
                val cachedVideos = videoDao.getTrendingVideos(20).first()
                if (cachedVideos.isNotEmpty()) {
                    Result.success(cachedVideos)
                } else {
                    fetchAndCacheTrendingVideos(page)
                }
            } else {
                fetchAndCacheTrendingVideos(page)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun fetchAndCacheTrendingVideos(page: Int): Result<List<Video>> {
        return try {
            val response = apiService.getTrendingVideos(page)
            if (page == 1) {
                videoDao.insertVideos(response.videos)
            }
            Result.success(response.videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVideosByCategory(category: String, page: Int = 1, forceRefresh: Boolean = false): Result<List<Video>> {
        return try {
            if (!forceRefresh) {
                val cachedVideos = videoDao.getVideosByCategory(category).first()
                if (cachedVideos.isNotEmpty()) {
                    Result.success(cachedVideos)
                } else {
                    fetchAndCacheVideosByCategory(category, page)
                }
            } else {
                fetchAndCacheVideosByCategory(category, page)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun fetchAndCacheVideosByCategory(category: String, page: Int): Result<List<Video>> {
        return try {
            val response = apiService.getVideosByCategory(category, page)
            if (page == 1) {
                videoDao.insertVideos(response.videos)
            }
            Result.success(response.videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchVideos(query: String, page: Int = 1): Result<List<Video>> {
        return try {
            val response = apiService.searchVideos(query, page)
            Result.success(response.videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVideoDetail(videoId: String): Result<Video> {
        return try {
            // First check local cache
            val cachedVideo = videoDao.getVideoById(videoId)
            if (cachedVideo != null) {
                Result.success(cachedVideo)
            } else {
                val video = apiService.getVideoDetail(videoId)
                videoDao.insertVideo(video)
                Result.success(video)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRelatedVideos(videoId: String): Result<List<Video>> {
        return try {
            val videos = apiService.getRelatedVideos(videoId)
            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCategories(): Result<List<com.videoapp.data.model.Category>> {
        return try {
            val categories = apiService.getCategories()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeTrendingVideos(): Flow<List<Video>> {
        return videoDao.getTrendingVideos(20)
    }
    
    fun observeVideosByCategory(category: String): Flow<List<Video>> {
        return videoDao.getVideosByCategory(category)
    }
    
    fun observeSearchResults(query: String): Flow<List<Video>> {
        return videoDao.searchVideos("%$query%")
    }
}
