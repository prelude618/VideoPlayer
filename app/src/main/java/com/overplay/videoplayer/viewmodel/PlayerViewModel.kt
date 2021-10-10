package com.overplay.videoplayer.viewmodel

import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.*
import com.overplay.videoplayer.Resource
import com.overplay.videoplayer.entity.Coordinates
import com.overplay.videoplayer.entity.LocationInfo
import com.overplay.videoplayer.usecase.DistanceGetter
import com.overplay.videoplayer.usecase.ShakeValueGetter
import kotlinx.coroutines.*

class PlayerViewModel(
        private val distanceGetter: DistanceGetter,
        private val shakeValueGetter: ShakeValueGetter
) : ViewModel() {
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

    fun isOverTenMeters(previousLocation: LocationInfo, currentLocation: LocationInfo): Boolean {
        Log.d(TAG, "LocationInfo = ${currentLocation.latitude}, ${currentLocation.longitude}")
        return distanceGetter.isOverTenMeters(previousLocation, currentLocation)
    }

    fun isOverThreshold(previousCoordinates: Coordinates, currentCoordinates: Coordinates, diffTime: Long): Boolean {
        return shakeValueGetter.isOverThreshold(previousCoordinates, currentCoordinates, diffTime)
    }

    override fun onCleared() {
        super.onCleared()
        jobDelay.cancel()
    }
}