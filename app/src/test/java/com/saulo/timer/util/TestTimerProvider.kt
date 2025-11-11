package com.saulo.timer.util

class TestTimerProvider : TimerProvider {
    private var onTick: ((Long) -> Unit)? = null
    private var onFinish: (() -> Unit)? = null

    override fun create(millisInFuture: Long, countDownInterval: Long, onTick: (Long) -> Unit, onFinish: () -> Unit): Timer {
        this.onTick = onTick
        this.onFinish = onFinish
        return TestTimer()
    }

    fun tick(remainingTime: Long) {
        onTick?.invoke(remainingTime)
    }

    fun finishTimer() {
        onFinish?.invoke()
    }

    inner class TestTimer : Timer {
        override fun start(): Timer {
            // Don't do anything, we control time manually
            return this
        }

        override fun cancel() {
            // Don't do anything
        }
    }
}
