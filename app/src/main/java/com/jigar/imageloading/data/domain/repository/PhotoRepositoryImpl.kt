package com.jigar.imageloading.data.domain.repository

import com.jigar.imageloading.data.ApiResult
import com.jigar.imageloading.data.api.PhotosApi
import com.jigar.imageloading.data.domain.model.Photo
import com.jigar.imageloading.extension.safeApiCall


class PhotoRepositoryImpl(private val photosApi: PhotosApi) : PhotoRepository {
    override suspend fun getPhotos(page: Int, clientId: String): ApiResult<List<Photo>> {
        return safeApiCall {
            photosApi.getPhotos(
                page = page, clientId = clientId
            )
        }
    }
}