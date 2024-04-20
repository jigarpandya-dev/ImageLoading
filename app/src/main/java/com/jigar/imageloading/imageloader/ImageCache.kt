package com.jigar.imageloading.imageloader

import android.graphics.Bitmap
import android.widget.ImageView

interface ImageCache {
    fun load(
        imageUrl: String,
        imageView: ImageView,
        placeholder: Int? = null,
        errorPlaceholder: Int? = null
    )

    suspend fun setImage(
        imageUrl: String, imageView: ImageView, bitmap: Bitmap?, errorPlaceholder: Int? = null
    )

    suspend fun setInMemory(key: String, bitmap: Bitmap)
    suspend fun getFromMemory(key: String): Bitmap?
    suspend fun getFileName(key: String): String
    suspend fun setInDisk(key: String, bitmap: Bitmap)
    suspend fun getFromDisk(key: String): Bitmap?
    suspend fun checkNetworkRequestInQueue(key: String):Boolean
    suspend fun removeNetworkRequestFromQueue(key: String)
    suspend fun getFromNetwork(key: String, callback: suspend (Bitmap?) -> Unit)
    suspend fun getCacheDirPath(): String
}