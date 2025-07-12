package com.example.wakeup.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wakeup.data.Quote
import com.example.wakeup.data.QuoteDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuoteViewModel(application: Application) : AndroidViewModel(application) {

    private val quoteDao = QuoteDatabase.getDatabase(application).quoteDao()
    private val firestore = FirebaseFirestore.getInstance()

    private val _quote = MutableStateFlow<Quote?>(null)
    val quote: StateFlow<Quote?> = _quote

    init {
        // Fetch from Room if Firestore fails, else sync
        viewModelScope.launch {
            syncQuotesFromFirestore()
            fetchRandomQuoteFromRoom()
        }
    }

    fun addQuote(text: String, author: String) {
        val quoteMap = hashMapOf(
            "text" to text,
            "author" to author,
            "liked" to false,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("quotes")
            .add(quoteMap)
            .addOnSuccessListener { documentRef ->
                Log.d("Firestore", "Quote added: ${documentRef.id}")
                val newQuote = Quote(id = documentRef.id, text = text, author = author, liked = false)
                viewModelScope.launch(Dispatchers.IO) {
                    quoteDao.insertQuote(newQuote)
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Add failed", it)
            }
    }

    fun toggleLikeForCurrentQuote() {
        val current = _quote.value
        if (!current?.id.isNullOrBlank()) {
            val updatedLiked = !current!!.liked
            firestore.collection("quotes")
                .document(current.id)
                .update("liked", updatedLiked)
                .addOnSuccessListener {
                    Log.d("Firestore", "Quote like toggled")
                    val updatedQuote = current.copy(liked = updatedLiked)
                    _quote.value = updatedQuote
                    viewModelScope.launch(Dispatchers.IO) {
                        quoteDao.updateQuote(updatedQuote)
                    }
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Failed to toggle like", it)
                }
        }
    }

    /** ✅ New: Sync Firestore to Room */
    fun syncQuotesFromFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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

                if (quotes.isNotEmpty()) {
                    quoteDao.deleteAll()             // 🧹 Clear local DB
                    quoteDao.insertAll(quotes)       // 🔄 Sync all
                    Log.d("Sync", "Synced ${quotes.size} quotes to Room")
                }

            } catch (e: Exception) {
                Log.e("Sync", "Firestore fetch failed", e)
            }
        }
    }

     fun fetchRandomQuoteFromRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            val localQuote = quoteDao.getRandomQuote()
            _quote.value = localQuote ?: Quote(
                text = "No quote found offline.",
                author = "WakeUp App"
            )
        }
    }

    fun insertDummyQuote() {
        viewModelScope.launch(Dispatchers.IO) {
            val dummy = Quote(
                id = "test123",
                text = "Believe in yourself, always.",
                author = "WakeUp Tester",
                liked = false
            )
            quoteDao.insertQuote(dummy)
        }
    }

    fun setQuote(quote: Quote) {
        _quote.value = quote
    }
}
