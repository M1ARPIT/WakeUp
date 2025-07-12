package com.example.wakeup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wakeup.data.QuoteDatabase
import com.example.wakeup.utils.NotificationUtils

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val db = QuoteDatabase.getDatabase(context)
        val quotes = db.quoteDao().getAllQuotes()
        val quote = if (quotes.isNotEmpty()) {
            val q = quotes.random()
            "\"${q.text}\" - ${q.author}"
        } else {
            "Keep going, you're doing great!"
        }

        NotificationUtils.showNotification(context,quote)
        return Result.success()
    }
}
