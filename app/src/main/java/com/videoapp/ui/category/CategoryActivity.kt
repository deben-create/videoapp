package com.videoapp.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.videoapp.R
import com.videoapp.databinding.ActivityCategoryBinding
import com.videoapp.ui.adapter.VideoAdapter
import com.videoapp.ui.detail.VideoDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCategoryBinding
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryListAdapter
    private lateinit var videoAdapter: VideoAdapter
    private var selectedCategoryId = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        selectedCategoryId = intent.getStringExtra("category_id") ?: ""
        
        setupToolbar()
        setupRecyclerViews()
        observeViewModel()
        viewModel.loadCategories()
        
        if (selectedCategoryId.isNotEmpty()) {
            viewModel.loadCategoryVideos(selectedCategoryId)
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        
        intent.getStringExtra("category_name")?.let { name ->
            supportActionBar?.title = name
        }
    }
    
    private fun setupRecyclerViews() {
        // Categories list (horizontal)
        categoryAdapter = CategoryListAdapter { category ->
            selectedCategoryId = category.id
            viewModel.loadCategoryVideos(category.id, true)
            supportActionBar?.title = category.name
        }
        
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoryActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
        
        // Videos grid
        videoAdapter = VideoAdapter { video ->
            val intent = Intent(this, VideoDetailActivity::class.java).apply {
                putExtra("video_id", video.id)
            }
            startActivity(intent)
        }
        
        binding.rvVideos.apply {
            layoutManager = GridLayoutManager(this@CategoryActivity, 2)
            adapter = videoAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.categories.observe(this@CategoryActivity) { categories ->
                categoryAdapter.submitList(categories)
            }
            
            viewModel.categoryVideos.observe(this@CategoryActivity) { videos ->
                videoAdapter.submitList(videos)
            }
            
            viewModel.isLoading.observe(this@CategoryActivity) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }
            
            viewModel.error.observe(this@CategoryActivity) { error ->
                error?.let {
                    // Show error
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

class CategoryListAdapter : androidx.recyclerview.widget.ListAdapter<com.videoapp.data.model.Category, CategoryListAdapter.CategoryViewHolder>(
    androidx.recyclerview.widget.DiffUtil.ItemCallback<com.videoapp.data.model.Category> { old, new ->
        old.id == new.id
    }
) {
    private var onClickListener: ((com.videoapp.data.model.Category) -> Unit)? = null
    
    constructor(onClickListener: (com.videoapp.data.model.Category) -> Unit) : this() {
        this.onClickListener = onClickListener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onClickListener)
    }
    
    class CategoryViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        private val tvName = view.findViewById<android.widget.TextView>(R.id.tvCategoryName)
        // private val ivIcon = view.findViewById<android.widget.ImageView>(R.id.ivCategoryIcon)
        
        fun bind(category: com.videoapp.data.model.Category, onClickListener: ((com.videoapp.data.model.Category) -> Unit)?) {
            tvName.text = category.name
            // Glide.with(view).load(category.iconUrl).into(ivIcon)
            
            view.setOnClickListener { onClickListener?.invoke(category) }
        }
    }
}
