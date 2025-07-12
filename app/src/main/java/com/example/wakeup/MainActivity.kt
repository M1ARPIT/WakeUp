package com.example.wakeup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wakeup.data.Quote
import com.example.wakeup.data.QuoteDatabase
import com.example.wakeup.ui.theme.WakeUpTheme
import com.example.wakeup.utils.PreferenceHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val msg = if (isGranted) "Notifications enabled!" else "Please enable notifications"
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        Log.d("Firebase", "Firebase initialized")

        requestNotificationPermission()
        createNotificationChannel()

        prepopulateQuotesIfNeeded()

        WorkScheduler.scheduleDailyMotivation(this)
        WorkScheduler.scheduleFrequentMotivation(this)
        scheduleFirestoreSync()

        // 🚀 Run only once to upload all local quotes to Firestore
//        uploadDefaultQuotesToFirestore() // 🛑 Comment this after 1st run

        setContent {
            WakeUpTheme {
                DrawerLayout()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun prepopulateQuotesIfNeeded() {
        if (PreferenceHelper.isFirstRun(this)) {
            val db = QuoteDatabase.getDatabase(this)
            CoroutineScope(Dispatchers.IO).launch {
                val defaultQuotes = getDefaultQuotes()
                defaultQuotes.forEach { (text, author) ->
                    val quote = Quote(
                        id = UUID.randomUUID().toString(),
                        text = text,
                        author = author,
                        liked = false
                    )
                    db.quoteDao().insertQuote(quote)
                }
                PreferenceHelper.setFirstRunCompleted(this@MainActivity)
            }
        }
    }

    private fun uploadDefaultQuotesToFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val defaultQuotes = getDefaultQuotes()

        defaultQuotes.forEach { (text, author) ->
            val quoteMap = hashMapOf(
                "text" to text,
                "author" to author,
                "liked" to false,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("quotes")
                .add(quoteMap)
                .addOnSuccessListener {
                    Log.d("Upload", "✅ Uploaded: $text")
                }
                .addOnFailureListener {
                    Log.e("Upload", "❌ Failed to upload", it)
                }
        }
    }

    private fun getDefaultQuotes(): List<Pair<String, String>> {
        return listOf(
            "Push yourself, because no one else is going to do it for you." to "Unknown",
            "Success doesn’t come from what you do occasionally, it comes from what you do consistently." to "Marie Forleo",
            "Doubt kills more dreams than failure ever will." to "Suzy Kassem",
            "Don’t watch the clock. Keep going." to "Sam Levenson",
            "The future depends on what you do today." to "Mahatma Gandhi",
            "Believe you can and you’re halfway there." to "Theodore Roosevelt",
            "Your limitation—it’s only your imagination." to "Unknown",
            "Great things never come from comfort zones." to "Unknown",
            "Dream it. Wish it. Do it." to "Unknown",
            "Stay positive, work hard, make it happen." to "Unknown",
            "Little by little, a little becomes a lot." to "Tanzanian Proverb",
            "Don’t limit your challenges. Challenge your limits." to "Jerry Dunn",
            "Difficult roads often lead to beautiful destinations." to "Zig Ziglar",
            "Work hard in silence. Let success be your noise." to "Frank Ocean",
            "Sometimes we’re tested not to show our weaknesses, but to discover our strengths." to "Unknown",
            "Believe in yourself and all that you are." to "Christian D. Larson",
            "It always seems impossible until it’s done." to "Nelson Mandela",
            "Success is what comes after you stop making excuses." to "Luis Galarza",
            "Failure is not the opposite of success; it’s part of success." to "Arianna Huffington",
            "You don’t have to be great to start, but you have to start to be great." to "Zig Ziglar",
            "Be stronger than your strongest excuse." to "Unknown",
            "Difficulties in life are intended to make us better, not bitter." to "Dan Reeves",
            "Hustle in silence and let your success make the noise." to "Unknown",
            "Stars can't shine without darkness." to "D.H. Sidebottom",
            "Act as if what you do makes a difference. It does." to "William James",
            "Opportunities don’t happen. You create them." to "Chris Grosser",
            "Discipline is doing what needs to be done even if you don’t want to do it." to "Unknown",
            "If you get tired, learn to rest, not to quit." to "Banksy",
            "Do what you can with all you have, wherever you are." to "Theodore Roosevelt",
            "What lies behind us and what lies before us are tiny matters compared to what lies within us." to "Ralph Waldo Emerson",
            "The way to get started is to quit talking and begin doing." to "Walt Disney",
            "You are never too old to set another goal or to dream a new dream." to "C.S. Lewis",
            "Make each day your masterpiece." to "John Wooden",
            "The only place where success comes before work is in the dictionary." to "Vidal Sassoon",
            "Start where you are. Use what you have. Do what you can." to "Arthur Ashe",
            "Success usually comes to those who are too busy to be looking for it." to "Henry David Thoreau",
            "If you want to lift yourself up, lift up someone else." to "Booker T. Washington",
            "Perseverance is not a long race; it is many short races one after the other." to "Walter Elliot",
            "If you can dream it, you can do it." to "Walt Disney",
            "Don’t be pushed around by the fears in your mind. Be led by the dreams in your heart." to "Roy T. Bennett",
            "The best way to predict the future is to create it." to "Peter Drucker",
            "Strength doesn’t come from what you can do. It comes from overcoming the things you once thought you couldn’t." to "Rikki Rogers",
            "Don’t count the days. Make the days count." to "Muhammad Ali",
            "Your only limit is your mind." to "Unknown",
            "Success is not for the lazy." to "Unknown",
            "Turn your wounds into wisdom." to "Oprah Winfrey",
            "You miss 100% of the shots you don’t take." to "Wayne Gretzky",
            "Everything you’ve ever wanted is on the other side of fear." to "George Addair",
            "Dare to dream, then decide to do." to "Annette White",
            "Wake up with determination. Go to bed with satisfaction." to "Unknown"
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "wake_up_channel"
            val channelName = "Motivational Quotes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for motivational quote notifications"
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleFirestoreSync() {
        val syncRequest = PeriodicWorkRequestBuilder<FirestoreSyncWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FirestoreSync",
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
        Log.d("MainActivity", "Firestore sync scheduled every 12 hours")
    }
}
