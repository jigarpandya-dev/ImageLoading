package com.jigar.imageloading.extension

import com.jigar.imageloading.data.ApiResult
import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
    try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            body?.let {
                return ApiResult.Success(body)
            }
        }
        return ApiResult.Error(
            message = "${response.code()} ${response.message()}"
        )
    } catch (e: Exception) {
        return when (e) {
            is UnknownHostException, is ConnectException -> {
                ApiResult.NoInternet
            }

            else -> {
                ApiResult.Error(
                    message = e.message.toString()
                )
            }
        }

    }
}

inline fun <T> ApiResult<T>.whenSuccess(function: (T) -> Unit): ApiResult<T> {
    (this as? ApiResult.Success<T>)?.let { data?.let { function(it) } }
    return this
}

inline fun <T> ApiResult<T>.whenLoading(function: () -> Unit): ApiResult<T> {
    (this as? ApiResult.Loading)?.let { function() }
    return this
}

inline fun <T> ApiResult<T>.whenNoInternet(function: () -> Unit): ApiResult<T> {
    (this as? ApiResult.NoInternet)?.let { function() }
    return this
}

inline fun <T> ApiResult<T>.whenError(function: (String?) -> Unit): ApiResult<T> {
    (this as? ApiResult.Error<T>)?.let { function(it.message) }
    return this
}
