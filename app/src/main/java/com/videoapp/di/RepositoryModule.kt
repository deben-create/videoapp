package com.videoapp.di

import com.videoapp.data.local.AppDatabase
import com.videoapp.data.local.VideoDao
import com.videoapp.data.repository.VideoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }
    
    @Provides
    @Singleton
    fun provideVideoRepository(videoDao: VideoDao): VideoRepository {
        // Note: This will be overridden by Hilt's constructor injection
        // but kept for explicitness
        return VideoRepository(
            // Will be provided by NetworkModule
        )
    }
}
