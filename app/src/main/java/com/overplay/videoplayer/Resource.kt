package com.overplay.videoplayer

sealed class Resource<T1, T2>(
    val data: T1? = null,
    val status: T2? = null,
    val message: String? = null
) {
    class Success<T1, T2>(data: T1, status: T2) : Resource<T1, T2>(data, status)
    class Loading<T1, T2>(data: T1? = null, status: T2) : Resource<T1, T2>(data, status)
    class Error<T1, T2>(message: String, data: T1? = null, status: T2) :
        Resource<T1, T2>(data, status, message)
}
