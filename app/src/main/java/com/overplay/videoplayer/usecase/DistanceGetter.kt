package com.overplay.videoplayer.usecase

import com.overplay.videoplayer.entity.LocationInfo
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DistanceGetter() {
    companion object {
        private const val TAG = "DistanceGetter"
        private const val METER_THRESHOLD = 10
    }

    fun isOverTenMeters(previousLocation: LocationInfo, currentLocation: LocationInfo): Boolean {
        return getDistance(previousLocation, currentLocation) > METER_THRESHOLD
    }

    private fun getDistance(previousLocation: LocationInfo, currentLocation: LocationInfo): Int {
        val radius = 6371
        val dLat = deg2rad(currentLocation.latitude - previousLocation.latitude)
        val dLon = deg2rad(currentLocation.longitude - previousLocation.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(deg2rad(previousLocation.latitude)) * cos(deg2rad(currentLocation.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        val result: Double = (2 * atan2(sqrt(a), sqrt(1 - a)) * radius * 1000)
        return if (result > Int.MAX_VALUE.toDouble()) {
            METER_THRESHOLD + 1
        } else {
            result.toInt()
        }
    }

    private fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

}