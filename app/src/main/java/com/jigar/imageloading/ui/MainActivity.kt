package com.jigar.imageloading.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.jigar.imageloading.R
import com.jigar.imageloading.extension.whenError
import com.jigar.imageloading.extension.whenLoading
import com.jigar.imageloading.extension.whenNoInternet
import com.jigar.imageloading.extension.whenSuccess
import com.jigar.imageloading.imageloader.ImageCache
import com.jigar.imageloading.ui.adapter.PhotosAdapter
import com.jigar.imageloading.viewmodel.PhotoViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val photoViewModel: PhotoViewModel by viewModels()
    private var isRefresh = false

    @Inject
    lateinit var imageCache: ImageCache
    private lateinit var photosAdapter: PhotosAdapter
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photosAdapter = PhotosAdapter(imageCache)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initComponent()
        initObserver()
        photoViewModel.getPhotos()
    }

    private fun initComponent() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        photosRecyclerView = findViewById(R.id.photosRecyclerView)
        photosRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = photosAdapter
        }
        photosRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = photosRecyclerView.layoutManager as? GridLayoutManager
                val visibleItemCount: Int = layoutManager?.childCount ?: 0
                val totalItemCount: Int? = layoutManager?.getItemCount() ?: 0
                val firstVisibleItemPosition: Int =
                    layoutManager?.findFirstVisibleItemPosition() ?: 0
                if ((visibleItemCount + firstVisibleItemPosition) >= (totalItemCount
                        ?: 0) && firstVisibleItemPosition >= 0 && (totalItemCount ?: 0) >= 10
                ) {
                    photoViewModel.loadMore()
                }
            }
        })
    }

    private val refreshListener = SwipeRefreshLayout.OnRefreshListener {
        isRefresh = true
        photoViewModel.refresh()
    }

    private fun initObserver() {
        photoViewModel.photosLiveData.observe(this) { result ->
            result.whenLoading {
                swipeRefreshLayout.setOnRefreshListener(null)
                swipeRefreshLayout.isRefreshing = true
            }.whenSuccess {
                swipeRefreshLayout.isRefreshing = false
                if (isRefresh) {
                    photosAdapter.clear()
                    isRefresh = false
                }
                photosAdapter.addAllItems(it.toMutableList())
                swipeRefreshLayout.setOnRefreshListener(refreshListener)
            }.whenError {
                swipeRefreshLayout.setOnRefreshListener(refreshListener)
                swipeRefreshLayout.isRefreshing = false
                it?.let {
                    Snackbar.make(
                        this@MainActivity, photosRecyclerView, it, Snackbar.LENGTH_SHORT
                    ).show()
                }

            }.whenNoInternet {
                swipeRefreshLayout.setOnRefreshListener(refreshListener)
                swipeRefreshLayout.isRefreshing = false
                Snackbar.make(
                    this@MainActivity,
                    photosRecyclerView,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}