package com.overplay.videoplayer.usecase

import android.util.Log
import com.overplay.videoplayer.entity.Coordinate
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin

class ShakeValueGetterTest {
    @InjectMockKs
    private lateinit var shakeValueGetter: ShakeValueGetter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun test_getSpeedNormalCase() {
        val previousCoordinate = Coordinate(1f, 1f, 1f)
        val currentCoordinate = Coordinate(2f, 2f, 2f)
        val diffTime = 101L

        mockkStatic(Log::class)
        every { Log.d(any(), any())} returns 0

        assertEquals(297, shakeValueGetter.getSpeed(previousCoordinate, currentCoordinate, diffTime))
    }

    @Test
    fun test_getSpeedOverMaxInt() {
        val previousCoordinate = Coordinate(1f, 1f, 1f)
        val currentCoordinate = Coordinate(10000000f, 10000000f, 10000000f)
        val diffTime = 101L

        mockkStatic(Log::class)
        every { Log.d(any(), any())} returns 0

        assertEquals(2001, shakeValueGetter.getSpeed(previousCoordinate, currentCoordinate, diffTime))
    }
}