package com.jigar.imageloading.data.api

import com.jigar.imageloading.constant.Constant
import com.jigar.imageloading.data.domain.model.Photo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotosApi {
    @GET(Constant.API_PHOTOS)
    suspend fun getPhotos(
        @Query("page") page: Int,
        @Query("client_id") clientId: String,
    ): Response<List<Photo>>
}