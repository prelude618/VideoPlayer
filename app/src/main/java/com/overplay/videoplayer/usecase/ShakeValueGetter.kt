package com.overplay.videoplayer.usecase

import android.util.Log
import com.overplay.videoplayer.entity.Coordinates
import kotlin.math.abs

class ShakeValueGetter() {
    companion object {
        private const val TAG = "ShakeValueGetter"
        private const val SHAKE_THRESHOLD = 2000
    }

    fun isOverThreshold(previousCoordinates: Coordinates, currentCoordinates: Coordinates, previousTime: Long): Boolean {
        return getSpeed(previousCoordinates, currentCoordinates, previousTime) > SHAKE_THRESHOLD
    }

    private fun getSpeed(previousCoordinates: Coordinates, currentCoordinates: Coordinates, diffTime: Long): Int {
        val speed: Float = abs(currentCoordinates.x + currentCoordinates.y + currentCoordinates.z
                - previousCoordinates.x - previousCoordinates.y - previousCoordinates.z) / diffTime * 10000
        Log.d(TAG, "speed = $speed")
        return if (speed > Int.MAX_VALUE.toDouble()) {
            SHAKE_THRESHOLD + 1
        } else {
            speed.toInt()
        }
    }
}