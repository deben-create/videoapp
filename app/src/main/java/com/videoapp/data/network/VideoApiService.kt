package com.videoapp.data.network

import com.videoapp.data.model.Category
import com.videoapp.data.model.Video
import com.videoapp.data.model.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface VideoApiService {
    @GET("videos")
    suspend fun getTrendingVideos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): VideoResponse
    
    @GET("videos")
    suspend fun getVideosByCategory(
        @Query("category") category: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): VideoResponse
    
    @GET("videos/search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): VideoResponse
    
    @GET("videos/{id}")
    suspend fun getVideoDetail(
        @retrofit2.http.Path("id") id: String
    ): Video
    
    @GET("categories")
    suspend fun getCategories(): List<Category>
    
    @GET("videos/related")
    suspend fun getRelatedVideos(
        @Query("videoId") videoId: String,
        @Query("limit") limit: Int = 10
    ): List<Video>
}
