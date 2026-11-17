package com.videoapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.videoapp.data.model.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<Video>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video)
    
    @Query("SELECT * FROM videos WHERE category = :category ORDER BY publishedAt DESC")
    fun getVideosByCategory(category: String): Flow<List<Video>>
    
    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): Video?
    
    @Query("SELECT * FROM videos ORDER BY publishedAt DESC LIMIT :limit")
    fun getTrendingVideos(limit: Int): Flow<List<Video>>
    
    @Query("SELECT * FROM videos WHERE title LIKE :query OR description LIKE :query")
    fun searchVideos(query: String): Flow<List<Video>>
    
    @Query("DELETE FROM videos WHERE publishedAt < :cutoffDate")
    suspend fun deleteOldVideos(cutoffDate: String)
    
    @Query("SELECT * FROM videos WHERE category IN (:categories)")
    fun getVideosByCategories(categories: List<String>): Flow<List<Video>>
}
