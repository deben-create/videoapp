# VideoApp - Android Video Streaming Application

A modern Android video streaming application built with Kotlin, following Material Design guidelines and modern Android architecture patterns.

## Features

- **Video Playback**: ExoPlayer integration for smooth video streaming
- **Trending Videos**: Browse trending content
- **Categories**: Browse videos by category
- **Search**: Search for videos
- **Video Details**: View video details, description, tags, and related videos
- **Offline Support**: Room database for caching videos
- **Dependency Injection**: Hilt for clean architecture
- **Modern UI**: Material Components, RecyclerView, ViewBinding

## Architecture

- **MVVM** (Model-View-ViewModel) with Clean Architecture
- **Repository Pattern** for data management
- **Hilt** for Dependency Injection
- **Coroutines & Flow** for asynchronous programming
- **Room** for local database
- **Retrofit** for network calls
- **ExoPlayer** for video playback
- **Glide** for image loading

## Project Structure

```
app/
├── src/main/
│   ├── java/com/videoapp/
│   │   ├── data/
│   │   │   ├── local/          # Room database, DAO
│   │   │   ├── model/          # Data models (Video, Category)
│   │   │   ├── network/        # Retrofit API, Network module
│   │   │   └── repository/     # Repository implementation
│   │   ├── di/                 # Hilt modules
│   │   ├── service/            # Media playback service
│   │   └── ui/
│   │       ├── main/           # MainActivity, MainViewModel
│   │       ├── player/         # VideoPlayerActivity, ViewModel
│   │       ├── detail/         # VideoDetailActivity, ViewModel
│   │       ├── search/         # SearchActivity, ViewModel
│   │       ├── category/       # CategoryActivity, ViewModel
│   │       └── adapter/        # RecyclerView adapters
│   ├── res/
│   │   ├── layout/             # XML layouts
│   │   ├── values/             # Strings, colors, themes
│   │   ├── drawable/           # Vector drawables, shapes
│   │   └── xml/                # File provider paths
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Dependencies

- AndroidX Core, AppCompat, Material, ConstraintLayout
- RecyclerView, Lifecycle, Navigation
- ExoPlayer 2.19.1
- Glide 4.16.0
- Retrofit 2.9.0 + Gson converter
- OkHttp 4.12.0 with logging interceptor
- Kotlin Coroutines 1.7.3
- Room 2.6.1
- Hilt 2.48
- JUnit, Espresso for testing

## Building

1. Open the project in Android Studio
2. Let Gradle sync complete
3. Build > Make Project (Ctrl+F9)
4. Run on device/emulator

## API Configuration

The app expects a REST API at `https://api.example.com/` with the following endpoints:

- `GET /videos` - Trending videos (query: page, limit)
- `GET /videos?category={category}` - Videos by category
- `GET /videos/search?q={query}` - Search videos
- `GET /videos/{id}` - Video detail
- `GET /categories` - List categories
- `GET /videos/related?videoId={id}` - Related videos

Update `NetworkModule.kt` to point to your actual API endpoint.

## License

MIT License
