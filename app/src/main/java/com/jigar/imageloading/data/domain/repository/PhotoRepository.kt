package com.jigar.imageloading.data.domain.repository

import com.jigar.imageloading.data.ApiResult
import com.jigar.imageloading.data.domain.model.Photo


interface PhotoRepository {
    suspend fun getPhotos(
        page: Int,
        clientId: String,
    ): ApiResult<List<Photo>>
}