package com.jigar.imageloading.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.collection.LruCache
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileInputStream
import java.io.FileOutputStream

class ImageLoader(private val context: Context) : ImageCache {
    private val okHttpClient: OkHttpClient by lazy { OkHttpClient() }
    private val imagesMemoryCache: LruCache<String, Bitmap>
    private val networkRequestQueue = HashSet<String>()

    init {
        // Initialize memory cache with 1/8th of the maximum memory
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        imagesMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Synchronized
    override fun load(
        imageUrl: String, imageView: ImageView, placeholder: Int?, errorPlaceholder: Int?
    ) {
        placeholder?.let {
            imageView.setImageResource(placeholder)
        } ?: run {
            imageView.setImageDrawable(null)
        }
        if (imageUrl.isBlank()) {
            errorPlaceholder?.let {
                imageView.setImageResource(it)

            }
            return
        }
        GlobalScope.launch {
            imageView.tag = imageUrl
            val bitmapFromMemory = getFromMemory(imageUrl)
            if (bitmapFromMemory != null) {
                setImage(imageUrl, imageView, bitmapFromMemory)
                return@launch
            }
            val bitmapFromDisk = getFromDisk(imageUrl)
            if (bitmapFromDisk != null) {
                setInMemory(imageUrl, bitmapFromDisk)
                setImage(imageUrl, imageView, bitmapFromDisk)
                return@launch
            }
            if (checkNetworkRequestInQueue(imageUrl)) {
                return@launch
            }
            getFromNetwork(imageUrl) {
                setImage(imageUrl, imageView, it, errorPlaceholder = errorPlaceholder)
            }
        }
    }

    override suspend fun setImage(
        imageUrl: String, imageView: ImageView, bitmap: Bitmap?, errorPlaceholder: Int?
    ) {
        withContext(Dispatchers.Main) {
            if (imageView.tag == imageUrl && bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                errorPlaceholder?.let {
                    imageView.setImageResource(it)

                } ?: run {
                    imageView.setImageDrawable(null)
                }
            }
        }
    }

    override suspend fun setInMemory(key: String, bitmap: Bitmap) {
        if (imagesMemoryCache.get(key) == null) {
            imagesMemoryCache.put(key, bitmap)
        }
    }

    override suspend fun getFromMemory(key: String): Bitmap? {
        return try {
            imagesMemoryCache.get(key)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getFileName(key: String): String {
        return key.replace("[^a-zA-Z0-9]".toRegex(), "")
    }

    override suspend fun setInDisk(key: String, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val filePath = "${getCacheDirPath()}/${getFileName(key)}"
            val fileOutputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
        }
    }

    override suspend fun getFromDisk(key: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val filePath = "${getCacheDirPath()}/${getFileName(key)}"
                val fileInputStream = FileInputStream(filePath)
                val bitmap = BitmapFactory.decodeStream(fileInputStream)
                fileInputStream.close()
                setInMemory(key, bitmap)
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun checkNetworkRequestInQueue(key: String): Boolean {
        return networkRequestQueue.contains(key)
    }

    override suspend fun removeNetworkRequestFromQueue(key: String) {
        networkRequestQueue.remove(key)
    }

    override suspend fun getFromNetwork(key: String, callback: suspend (Bitmap?) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val result = okHttpClient.newCall(Request.Builder().url(key).build()).execute()
                if (result.isSuccessful) {
                    val byteStream = result.body?.byteStream()
                    byteStream?.let {
                        val bitmap = BitmapFactory.decodeStream(it)
                        byteStream.close()
                        setInMemory(key, bitmap)
                        setInDisk(key, bitmap)
                        removeNetworkRequestFromQueue(key)
                        withContext(Dispatchers.Main) {
                            callback.invoke(bitmap)
                        }
                    } ?: run {
                        removeNetworkRequestFromQueue(key)
                        withContext(Dispatchers.Main) {
                            callback.invoke(null)
                        }
                    }
                } else {
                    removeNetworkRequestFromQueue(key)
                    withContext(Dispatchers.Main) {
                        callback.invoke(null)
                    }
                }
            } catch (e: Exception) {
                removeNetworkRequestFromQueue(key)
                withContext(Dispatchers.Main) {
                    callback.invoke(null)
                }
            }
        }
    }

    override suspend fun getCacheDirPath(): String {
        return context.cacheDir.absolutePath
    }

}