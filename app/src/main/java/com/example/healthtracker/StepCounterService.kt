package com.example.healthtracker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper

class StepCounterService : Service() {
    private lateinit var stepTracker: StepTracker
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // Update every 1 second

    companion object {
        const val ACTION_STEP_COUNT_UPDATE = "com.example.healthtracker.STEP_COUNT_UPDATE"
        const val EXTRA_STEP_COUNT = "extra_step_count"
    }

    override fun onCreate() {
        super.onCreate()
        stepTracker = StepTracker(this)
        stepTracker.startCounting()
        startStepCountUpdates()
    }

    private fun startStepCountUpdates() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                broadcastStepCount()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    private fun broadcastStepCount() {
        val intent = Intent(ACTION_STEP_COUNT_UPDATE)
        intent.putExtra(EXTRA_STEP_COUNT, stepTracker.getStepCount())
        sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stepTracker.stopCounting()
        handler.removeCallbacksAndMessages(null)
    }
}