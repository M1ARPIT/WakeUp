package com.example.wakeup.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "quotes")
data class Quote(
    @DocumentId // 🔥 Firestore se document ID auto le lega
    @PrimaryKey(autoGenerate = false)
    val id: String = "",

    val text: String = "",
    val author: String = "",
    val liked: Boolean = false
)
