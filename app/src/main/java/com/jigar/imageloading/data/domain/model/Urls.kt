package com.jigar.imageloading.data.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Urls(
    @Json(name = "full") val full: String?,
    @Json(name = "raw") val raw: String?,
    @Json(name = "regular") val regular: String?,
    @Json(name = "small") val small: String?,
    @Json(name = "small_s3") val smallS3: String?,
    @Json(name = "thumb") val thumb: String?
)