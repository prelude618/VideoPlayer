package com.overplay.videoplayer.viewmodel

import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.*
import com.overplay.videoplayer.Resource
import kotlinx.coroutines.*

class PlayerViewModel : ViewModel() {
    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val status: MutableLiveData<Resource<Unit, Unit>> = MutableLiveData()
    private var job: Job = Job()

    @UiThread
    fun playMedia(): LiveData<Resource<Unit, Unit>> {
        if(job.isActive) {
            job.cancel()
        }

        job = CoroutineScope(Dispatchers.Default).launch {
            delay(5000)
            status.postValue(Resource.Success(Unit, Unit))
        }

        return status
    }
}