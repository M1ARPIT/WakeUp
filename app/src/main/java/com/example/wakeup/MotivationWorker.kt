package com.example.wakeup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wakeup.data.Quote
import com.example.wakeup.data.QuoteDatabase
import com.example.wakeup.utils.NotificationUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MotivationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        Log.d("MotivationWorker", "Starting doWork")

        try {
            // 🔒 Handle notification permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionStatus = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    Log.d("MotivationWorker", "POST_NOTIFICATIONS permission not granted")
                    return Result.failure()
                }
            }

            val db = QuoteDatabase.getDatabase(context)
            val dao = db.quoteDao()

            // ✅ 1. Sync from Firestore → Room
            val snapshot = firestore.collection("quotes").get().await()
            val quotes = snapshot.documents.mapNotNull { doc ->
                val text = doc.getString("text")
                val author = doc.getString("author")
                val liked = doc.getBoolean("liked") ?: false
                val id = doc.id

                if (!text.isNullOrBlank() && !author.isNullOrBlank()) {
                    Quote(id = id, text = text, author = author, liked = liked)
                } else null
            }

            Log.d("MotivationWorker", "Fetched ${quotes.size} quotes from Firestore")

            // ✅ 2. Save to Room (replace all)
            withContext(Dispatchers.IO) {
                dao.deleteAll()
                dao.insertAll(quotes)
                Log.d("MotivationWorker", "Room DB updated with Firestore data")
            }

            // ✅ 3. Get Random Quote from local Room DB
            val quote = withContext(Dispatchers.IO) {
                dao.getRandomQuote() ?: Quote(id = "", text = "Stay motivated!", author = "Unknown")
            }

            val notificationText = "\"${quote.text}\" - ${quote.author}"
            NotificationUtils.showNotification(context, notificationText)

            Log.d("MotivationWorker", "Notification shown")
            return Result.success()

        } catch (e: Exception) {
            Log.e("MotivationWorker", "Error in doWork: ${e.message}", e)
            return Result.failure()
        }
    }
}
