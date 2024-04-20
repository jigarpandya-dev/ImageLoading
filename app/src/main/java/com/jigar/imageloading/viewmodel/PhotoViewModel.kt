package com.jigar.imageloading.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jigar.imageloading.constant.Constant
import com.jigar.imageloading.data.ApiResult
import com.jigar.imageloading.data.domain.model.Photo
import com.jigar.imageloading.data.domain.repository.PhotoRepository
import com.jigar.imageloading.extension.whenError
import com.jigar.imageloading.extension.whenNoInternet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(private val photoRepository: PhotoRepository) :
    ViewModel() {
    private var page = 1
    private var job: Job? = null
    private val _photosLiveData = MutableLiveData<ApiResult<List<Photo>>>()
    val photosLiveData: LiveData<ApiResult<List<Photo>>> = _photosLiveData

    fun getPhotos() {
        job = viewModelScope.launch {
            _photosLiveData.postValue(ApiResult.Loading)
            val result = photoRepository.getPhotos(page = page, clientId = Constant.CLIENT_ID)
            result.whenError {
                if (page > 1) {
                    page = page.minus(1)
                }
            }.whenNoInternet {
                if (page > 1) {
                    page = page.minus(1)
                }
            }
            _photosLiveData.postValue(result)
        }
    }

    fun loadMore() {
        if (job?.isActive == true) {
            return
        }
        page = page.plus(1)
        getPhotos()
    }

    fun refresh() {
        page = 1
        getPhotos()
    }
}