package com.example.wakeup

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wakeup.data.Quote
import com.example.wakeup.data.QuoteDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("quotes").get().await()

            val quotes = snapshot.documents.mapNotNull { doc ->
                val text = doc.getString("text") ?: return@mapNotNull null
                val author = doc.getString("author") ?: "Unknown"
                val liked = doc.getBoolean("liked") ?: false
                val id = doc.id
                Quote(id = id, text = text, author = author, liked = liked)
            }

            val dao = QuoteDatabase.getDatabase(context).quoteDao()
            dao.deleteAll()
            dao.insertAll(quotes)

            Log.d("FirestoreSyncWorker", "Synced ${quotes.size} quotes to Room DB.")
            Result.success()
        } catch (e: Exception) {
            Log.e("FirestoreSyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}
