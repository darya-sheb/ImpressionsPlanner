package com.hse.impressionsplanner.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hse.impressionsplanner.ui.auth.AuthScreen
import com.hse.impressionsplanner.ui.auth.AuthViewModel

@Composable
fun ProfileScreen(viewModel: AuthViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser == null) {
        AuthScreen(viewModel = viewModel)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Профиль",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = currentUser!!.email ?: "",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(onClick = { viewModel.signOut() }) {
                Text("Выйти")
            }
        }
    }
}