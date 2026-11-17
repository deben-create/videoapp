package com.videoapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.videoapp.R
import com.videoapp.data.model.Category
import com.videoapp.data.model.Video
import com.videoapp.databinding.ActivityMainBinding
import com.videoapp.databinding.ItemCategoryBinding
import com.videoapp.ui.adapter.VideoAdapter
import com.videoapp.ui.adapter.navigateToVideoPlayer
import com.videoapp.ui.category.CategoryActivity
import com.videoapp.ui.detail.VideoDetailActivity
import com.videoapp.ui.player.VideoPlayerRouterActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }
    
    private fun setupRecyclerViews() {
        // Trending videos - horizontal list
        videoAdapter = VideoAdapter { video ->
            navigateToVideoPlayer(this, video)
        }
        
        binding.rvTrending.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = videoAdapter
            setHasFixedSize(true)
        }
        
        // Categories - horizontal list
        categoryAdapter = CategoryAdapter { category ->
            val intent = Intent(this, CategoryActivity::class.java).apply {
                putExtra("category_id", category.id)
                putExtra("category_name", category.name)
            }
            startActivity(intent)
        }
        
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
        
        // Recommended videos - grid
        val recommendedAdapter = VideoAdapter { video ->
            navigateToVideoPlayer(this, video)
        }
        
        binding.rvRecommended.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = recommendedAdapter
            setHasFixedSize(true)
        }
        
        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.trendingVideos.observe(this@MainActivity) { videos ->
                videoAdapter.submitList(videos)
                binding.swipeRefresh.isRefreshing = false
            }
            
            viewModel.categories.observe(this@MainActivity) { categories ->
                categoryAdapter.submitList(categories)
            }
            
            viewModel.isLoading.observe(this@MainActivity) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }
            
            viewModel.error.observe(this@MainActivity) { error ->
                error?.let {
                    binding.tvError.text = it
                    binding.tvError.isVisible = true
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        
        binding.tvSeeAllCategories.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }
        
        binding.tvSeeAllTrending.setOnClickListener {
            // Navigate to all trending videos
        }
        
        binding.tvSeeAllRecommended.setOnClickListener {
            // Navigate to all recommended videos
        }
    }
}

class CategoryAdapter : androidx.recyclerview.widget.ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(
    androidx.recyclerview.widget.DiffUtil.ItemCallback<Category> { old, new ->
        old.id == new.id
    }
) {
    private var onClickListener: ((Category) -> Unit)? = null
    
    constructor(onClickListener: (Category) -> Unit) : this() {
        this.onClickListener = onClickListener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onClickListener)
    }
    
    class CategoryViewHolder(private val binding: ItemCategoryBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category, onClickListener: ((Category) -> Unit)?) {
            binding.tvCategoryName.text = category.name
            // Load category icon using Glide
            // Glide.with(binding.root).load(category.iconUrl).into(binding.ivCategoryIcon)
            
            binding.root.setOnClickListener { onClickListener?.invoke(category) }
        }
    }
}
