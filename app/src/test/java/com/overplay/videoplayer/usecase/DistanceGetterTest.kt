package com.overplay.videoplayer.usecase

import com.overplay.videoplayer.entity.LocationInfo
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin

class DistanceGetterTest {
    @InjectMockKs
    private lateinit var distanceGetter: DistanceGetter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun test_getDistanceNormalCase() {
        val previousLocation = LocationInfo(0.0, 0.0)
        val currentLocation = LocationInfo(0.1, 0.1)

        assertEquals(15725, distanceGetter.getDistance(previousLocation, currentLocation))
    }

    @Test
    fun test_getDistanceSamePositionWithDifferentLongitudes() {
        val previousLocation = LocationInfo(0.0, -180.0)
        val currentLocation = LocationInfo(0.0, 180.0)

        assertEquals(0, distanceGetter.getDistance(previousLocation, currentLocation))
    }

    @Test
    fun test_degreeToRadian() {
        val deg = 90.0

        assertEquals(1.5708, distanceGetter.degreeToRadian(deg), 0.0001)
    }

}