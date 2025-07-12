package com.example.wakeup

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wakeup.viewmodel.QuoteViewModel
import com.example.wakeup.viewmodel.ViewModelFactory

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel: QuoteViewModel =
        viewModel(factory = ViewModelFactory(context.applicationContext as Application))

    val quote by viewModel.quote.collectAsState()

    // Show loader if quote is still null
    if (quote == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Main UI when quote is available
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = quote!!.text.ifBlank { "Stay strong, keep going!" },
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "- ${quote!!.author.ifBlank { "Unknown" }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val liked = quote!!.liked
                        IconButton(onClick = {
                            viewModel.toggleLikeForCurrentQuote()
                        }) {
                            Icon(
                                imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (liked) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(if (liked) "Liked!" else "Like this quote")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.fetchRandomQuoteFromRoom()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh")
            }

        }
    }
}
