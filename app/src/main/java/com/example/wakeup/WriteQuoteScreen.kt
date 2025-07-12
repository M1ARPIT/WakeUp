package com.example.wakeup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wakeup.viewmodel.QuoteViewModel
import com.example.wakeup.viewmodel.ViewModelFactory

@Composable
fun WriteQuoteScreen() {
    val context = LocalContext.current
    val viewModel: QuoteViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    var quoteText by remember { mutableStateOf("") }
    var authorText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = quoteText,
            onValueChange = { quoteText = it },
            label = { Text("Quote") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = authorText,
            onValueChange = { authorText = it },
            label = { Text("Author (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val quoteTrimmed = quoteText.trim()
                val authorTrimmed = authorText.trim()
                if (quoteTrimmed.isNotEmpty()) {
                    viewModel.addQuote(quoteTrimmed, authorTrimmed)

                    quoteText = ""
                    authorText = ""
                    Toast.makeText(context, "Quote saved!", Toast.LENGTH_SHORT).show()
                }
            }
            ,
            enabled = quoteText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Quote")
        }
    }
}
