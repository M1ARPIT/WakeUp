package com.example.wakeup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wakeup.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.wakeup.data.QuoteDatabase

class WakeUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = QuoteDatabase.getDatabase(context).quoteDao()
            val quotes = dao.getAllQuotes()
            val quoteText = if (quotes.isNotEmpty()) {
                val randomQuote = quotes.random()
                "“${randomQuote.text}” - ${randomQuote.author}"
            } else {
                "“Stay positive, work hard, make it happen!”"
            }

            NotificationUtils.showNotification(context, quoteText)
        }
    }
}
