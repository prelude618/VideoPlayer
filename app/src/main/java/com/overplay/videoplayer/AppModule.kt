package com.overplay.videoplayer

import com.overplay.videoplayer.usecase.DistanceGetter
import com.overplay.videoplayer.viewmodel.PlayerViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { DistanceGetter(get(), get()) }
    viewModel { PlayerViewModel() }
}