package com.bignerdranch.android.pomodoro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {
    private lateinit var binding: ActivityMainBinding

    private val timerAdapter = TimerAdapter(this)
    private val timers = arrayListOf<Timer>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null){
            nextId = savedInstanceState.getInt(NEXT_ID)
            val size = savedInstanceState.getInt(SIZE_TIMERS)
            for (i in 0 until size){
                val id = savedInstanceState.getInt("$ID$i")
                val currentMs = savedInstanceState.getLong("$MS$i")
                val isStarted = savedInstanceState.getBoolean("$START$i")
                timers.add(Timer(id, currentMs, isStarted))
            }
            timerAdapter.submitList(timers.toList())
        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            if (timers.size <= 1000) {
                val hours: Int? = binding.enterHoursEditText.text.toString().toIntOrNull()
                val minutes: Int? = binding.enterMinutesEditText.text.toString().toIntOrNull()
                val seconds: Int? = binding.enterSecondsEditText.text.toString().toIntOrNull()
                if (hours != null && minutes != null && seconds != null)
                    if (hours != 0 || minutes != 0 || seconds != 0){
                        val milliSeconds: Int = (hours * 3600 + minutes * 60 + seconds) * 1000
                        timers.add(Timer(nextId++, milliSeconds.toLong(), false))
                        timerAdapter.submitList(timers.toList())
                    }
            }
        }
    }

    override fun start(id: Int) {
        changeTimer(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeTimer(id, currentMs, false)
    }

    override fun delete(id: Int) {
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }

    private fun changeTimer(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            newTimers.add(
                if (it.id == id)
                    Timer(it.id, currentMs ?: it.currentMs, isStarted)
                else it
            )
        }
        timerAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(NEXT_ID, nextId)
        outState.putInt(SIZE_TIMERS, timers.size)
        for (i in timers.indices){
            outState.putInt("$ID$i", timers[i].id)
            outState.putLong("$MS$i", timers[i].currentMs)
            outState.putBoolean("$START$i", timers[i].isStarted)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, 0)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private companion object {
        const val NEXT_ID = "nextID"
        const val ID = "id"
        const val MS = "ms"
        const val START = "start"
        const val SIZE_TIMERS = "size of timers"
    }
}