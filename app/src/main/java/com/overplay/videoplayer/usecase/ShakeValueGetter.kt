package com.overplay.videoplayer.usecase

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.overplay.videoplayer.entity.Coordinate
import kotlin.math.abs

class ShakeValueGetter() {
    companion object {
        private const val TAG = "ShakeValueGetter"
        private const val SHAKE_THRESHOLD = 2000
    }

    fun isOverThreshold(
        previousCoordinate: Coordinate,
        currentCoordinate: Coordinate,
        previousTime: Long
    ): Boolean {
        return getSpeed(previousCoordinate, currentCoordinate, previousTime) > SHAKE_THRESHOLD
    }

    @VisibleForTesting
    fun getSpeed(
        previousCoordinate: Coordinate,
        currentCoordinate: Coordinate,
        diffTime: Long
    ): Int {
        val speed: Float = abs(
            currentCoordinate.x + currentCoordinate.y + currentCoordinate.z
                    - previousCoordinate.x - previousCoordinate.y - previousCoordinate.z
        ) / diffTime * 10000
        Log.d(TAG, "speed = $speed")
        return if (speed > Int.MAX_VALUE.toFloat()) {
            SHAKE_THRESHOLD + 1
        } else {
            speed.toInt()
        }
    }
}