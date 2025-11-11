package com.saulo.timer.util

interface TimerProvider {
    fun create(millisInFuture: Long, countDownInterval: Long, onTick: (Long) -> Unit, onFinish: () -> Unit): Timer
}

interface Timer {
    fun start(): Timer
    fun cancel()
}
