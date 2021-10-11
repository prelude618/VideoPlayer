package com.overplay.videoplayer.viewmodel

import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.*
import com.overplay.videoplayer.Resource
import com.overplay.videoplayer.entity.Coordinate
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
            delay(4000)
            delayStatus.postValue(Resource.Success(Unit, Unit))
        }

        return delayStatus
    }

    fun isOverTenMeters(previousLocation: LocationInfo, currentLocation: LocationInfo): Boolean {
        Log.d(TAG, "LocationInfo = ${currentLocation.latitude}, ${currentLocation.longitude}")
        return distanceGetter.isOverTenMeters(previousLocation, currentLocation)
    }

    fun isShakeOverThreshold(
        previousCoordinate: Coordinate,
        currentCoordinate: Coordinate,
        diffTime: Long
    ): Boolean {
        return shakeValueGetter.isOverThreshold(previousCoordinate, currentCoordinate, diffTime)
    }

    override fun onCleared() {
        super.onCleared()
        jobDelay.cancel()
    }
}