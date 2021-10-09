package com.overplay.videoplayer.viewmodel

import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.*
import com.overplay.videoplayer.Resource
import com.overplay.videoplayer.usecase.DistanceGetter
import kotlinx.coroutines.*

class PlayerViewModel : ViewModel() {
    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val delayStatus: MutableLiveData<Resource<Unit, Unit>> = MutableLiveData()
    private var jobDelay: Job = Job()

    @UiThread
    fun playMedia(): LiveData<Resource<Unit, Unit>> {
        if (jobDelay.isActive) {
            jobDelay.cancel()
        }

        jobDelay = CoroutineScope(Dispatchers.Default).launch {
            delay(5000)
            delayStatus.postValue(Resource.Success(Unit, Unit))
        }

        return delayStatus
    }

    fun isOverTenMeters(distanceGetter: DistanceGetter): Boolean {
        return distanceGetter.isOverTenMeters()
    }

    override fun onCleared() {
        super.onCleared()
        jobDelay.cancel()
    }
}