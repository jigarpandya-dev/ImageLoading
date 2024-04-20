package com.jigar.imageloading.data


sealed class ApiResult<out T>(
    val data: T? = null, val message: String? = null
) {
    class Success<T>(data: T) : ApiResult<T>(data)
    class Error<T>(
        message: String
    ) : ApiResult<T>(message = message)

    data object Loading : ApiResult<Nothing>()
    data object NoInternet : ApiResult<Nothing>()
}