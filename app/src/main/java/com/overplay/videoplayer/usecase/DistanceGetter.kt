package com.overplay.videoplayer.usecase

import com.overplay.videoplayer.entity.LocationInfo
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DistanceGetter(
    private val previousLocation: LocationInfo,
    private val currentLocation: LocationInfo
) {
    fun isOverTenMeters(): Boolean {
        return getDistance() > 10
    }

    private fun getDistance(): Int {
        val radius = 6371
        val dLat = deg2rad(currentLocation.latitude - previousLocation.latitude)
        val dLon = deg2rad(currentLocation.longitude - previousLocation.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(deg2rad(previousLocation.latitude)) * cos(deg2rad(currentLocation.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        return (2 * atan2(sqrt(a), sqrt(1 - a)) * radius * 1000).toInt()
    }

    private fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

}