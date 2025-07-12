package com.example.wakeup.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class QuoteRepository {

    private val db = FirebaseFirestore.getInstance()
    private val quotesCollection = db.collection("quotes")

    suspend fun getAllQuotes(): List<Quote> {
        val snapshot = quotesCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject<Quote>() }
    }

    suspend fun likeQuote(quoteId: String) {
        val docRef = quotesCollection.document(quoteId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentLikes = snapshot.getLong("likes") ?: 0
            transaction.update(docRef, "likes", currentLikes + 1)
        }.await()
    }
}
