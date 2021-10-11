package com.overplay.videoplayer.usecase

import androidx.annotation.VisibleForTesting
import com.overplay.videoplayer.entity.LocationInfo
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DistanceGetter() {
    companion object {
        private const val TAG = "DistanceGetter"
        private const val METER_THRESHOLD = 10
        private const val RADIUS = 6371
    }

    fun isOverTenMeters(previousLocation: LocationInfo, currentLocation: LocationInfo): Boolean {
        return getDistance(previousLocation, currentLocation) > METER_THRESHOLD
    }

    @VisibleForTesting
    fun getDistance(previousLocation: LocationInfo, currentLocation: LocationInfo): Int {
        val dLat = degreeToRadian(currentLocation.latitude - previousLocation.latitude)
        val dLon = degreeToRadian(currentLocation.longitude - previousLocation.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(degreeToRadian(previousLocation.latitude)) * cos(degreeToRadian(currentLocation.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        val result: Double = (2 * atan2(sqrt(a), sqrt(1 - a)) * RADIUS * 1000)
        return result.toInt()
    }

    @VisibleForTesting
    fun degreeToRadian(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

}