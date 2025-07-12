package com.example.wakeup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wakeup.data.Quote
import com.example.wakeup.data.QuoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QuoteListScreen(database: QuoteDatabase) {
    var quoteList by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    suspend fun loadQuotes() {
        isLoading = true
        quoteList = withContext(Dispatchers.IO) {
            database.quoteDao().getAllQuotes()
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadQuotes()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = {
                coroutineScope.launch {
                    loadQuotes()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text("Refresh Quotes")
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(quoteList) { quote ->
                    QuoteCard(quote)
                }
            }
        }
    }
}

@Composable
fun QuoteCard(quote: Quote) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "- ${quote.author}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
