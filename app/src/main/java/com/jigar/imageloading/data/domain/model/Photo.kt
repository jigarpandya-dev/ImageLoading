package com.jigar.imageloading.data.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Photo(
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "id") val id: String?,
    @Json(name = "updated_at") val updatedAt: String?,
    @Json(name = "urls") val urls: Urls?
)