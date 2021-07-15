package com.bignerdranch.android.pomodoro

data class Timer(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)