package com.jigar.imageloading.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jigar.imageloading.R
import com.jigar.imageloading.data.domain.model.Photo
import com.jigar.imageloading.imageloader.ImageCache

class PhotosAdapter(private val imageLoader: ImageCache) :
    RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {
    private val photos = mutableListOf<Photo>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSample: ImageView

        init {
            ivSample = view.findViewById(R.id.ivPhoto)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.row_photo, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        imageLoader.load(
            imageUrl = photos[position].urls?.thumb.orEmpty(), imageView = viewHolder.ivSample
        )
    }

    override fun getItemCount() = photos.size

    fun addAllItems(photos: MutableList<Photo>) {
        val startPosition = this.photos.lastIndex
        this.photos.addAll(photos)
        notifyItemRangeChanged(startPosition, this.photos.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        this.photos.clear()
        notifyDataSetChanged()
    }
}
