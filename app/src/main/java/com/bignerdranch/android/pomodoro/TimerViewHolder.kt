package com.bignerdranch.android.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.pomodoro.databinding.TimerItemBinding

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {
    private var timer: CountDownTimer? = null

    fun bind(timer: Timer) {
        binding.timerTextView.text = timer.currentMs.displayTime()

        if (timer.isStarted)
            startTimer(timer)
        else
            stopTimer()

        binding.tomatoProgress.setPeriod(timer.currentMs)

        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startPauseButton.setOnClickListener {
            val startedPosition = TimerAdapter.Start.startedPosition

            when {
                timer.isStarted -> {
                    if (timer.id == startedPosition){
                        listener.stop(timer.id, timer.currentMs)
                        TimerAdapter.Start.startedPosition = -1
                    }
                }
                else -> {
                    if (startedPosition == -1){
                        TimerAdapter.Start.startedPosition = timer.id
                        listener.start(timer.id)
                    }
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            listener.delete(timer.id)
        }
    }

    private fun startTimer(timer: Timer) {
        binding.startPauseButton.text = resources.getString(R.string.stop)

        this.timer?.cancel()
        this.timer = getCountDownTimer(timer)
        this.timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer() {
        binding.startPauseButton.text = resources.getString(R.string.start)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer =
        object : CountDownTimer(PERIOD, UNIT_ONE_HUNDRED_MS) {
            val interval = UNIT_ONE_HUNDRED_MS

            override fun onTick(millisUntilFinished: Long) {
                if (timer.currentMs <= 0L) {
                    stopTimer()
                    TimerAdapter.Start.startedPosition = -1
                }

                timer.currentMs -= interval
                binding.timerTextView.text = timer.currentMs.displayTime()
                binding.tomatoProgress.setCurrent(timer.currentMs)
            }

            override fun onFinish() {
                binding.timerTextView.text = timer.currentMs.displayTime()
                binding.tomatoProgress.setCurrent(0)
            }
        }

    private companion object {
        private const val UNIT_ONE_HUNDRED_MS = 100L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}