package com.saulo.timer.util

import android.os.CountDownTimer

class AndroidTimerProvider : TimerProvider {
    override fun create(millisInFuture: Long, countDownInterval: Long, onTick: (Long) -> Unit, onFinish: () -> Unit): Timer {
        return object : Timer {
            private val countDownTimer = object : CountDownTimer(millisInFuture, countDownInterval) {
                override fun onTick(millisUntilFinished: Long) {
                    onTick(millisUntilFinished)
                }

                override fun onFinish() {
                    onFinish()
                }
            }

            override fun start(): Timer {
                countDownTimer.start()
                return this
            }

            override fun cancel() {
                countDownTimer.cancel()
            }
        }
    }
}
