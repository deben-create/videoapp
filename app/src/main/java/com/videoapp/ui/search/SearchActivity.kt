package com.videoapp.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observer
import androidx.recyclerview.widget.GridLayoutManager
import com.videoapp.R
import com.videoapp.databinding.ActivitySearchBinding
import com.videoapp.ui.adapter.VideoAdapter
import com.videoapp.ui.detail.VideoDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAdapter: VideoAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupSearch()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
    
    private fun setupRecyclerView() {
        searchAdapter = VideoAdapter { video ->
            val intent = Intent(this, VideoDetailActivity::class.java).apply {
                putExtra("video_id", video.id)
            }
            startActivity(intent)
        }
        
        binding.rvSearchResults.apply {
            layoutManager = GridLayoutManager(this@SearchActivity, 2)
            adapter = searchAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    viewModel.search(query)
                } else if (query.isEmpty()) {
                    searchAdapter.submitList(emptyList())
                    binding.tvNoResults.isVisible = false
                    binding.rvSearchResults.isVisible = false
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.searchResults.observe(this@SearchActivity) { videos ->
                if (videos.isEmpty()) {
                    binding.tvNoResults.isVisible = true
                    binding.rvSearchResults.isVisible = false
                } else {
                    binding.tvNoResults.isVisible = false
                    binding.rvSearchResults.isVisible = true
                }
                searchAdapter.submitList(videos)
            }
            
            viewModel.isLoading.observe(this@SearchActivity) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }
            
            viewModel.error.observe(this@SearchActivity) { error ->
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
