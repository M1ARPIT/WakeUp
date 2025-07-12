package com.example.wakeup

import android.content.Context

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    fun scheduleDailyMotivation(context: Context) {
        val request = PeriodicWorkRequestBuilder<MotivationWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyMotivation",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
    fun scheduleReminderWork(context: Context, intervalMillis: Long) {
        val workManager = WorkManager.getInstance(context)

        // Cancel existing reminder if any
        workManager.cancelUniqueWork("reminder_worker")

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalMillis, TimeUnit.MILLISECONDS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "reminder_worker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun scheduleFrequentMotivation(context: Context) {
        // Use PeriodicWorkRequest for testing (15 minutes)
        val request = PeriodicWorkRequestBuilder<MotivationWorker>(
            15, TimeUnit.MINUTES // Change to 30 seconds for faster testing if needed
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "TestMotivation",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
}