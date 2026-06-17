package com.hse.impressionsplanner.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hse.impressionsplanner.ui.auth.AuthScreen
import com.hse.impressionsplanner.ui.auth.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        AuthScreen(viewModel = authViewModel)
        return
    }

    val displayName    by profileViewModel.displayName.collectAsState()
    val avatarEmoji    by profileViewModel.avatarEmoji.collectAsState()
    val isLoading      by profileViewModel.isLoading.collectAsState()
    val editingName    by profileViewModel.editingName.collectAsState()
    val nameInput      by profileViewModel.nameInput.collectAsState()
    val showEmojiPicker by profileViewModel.showEmojiPicker.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Аватарка-эмодзи
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { profileViewModel.openEmojiPicker() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarEmoji,
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { profileViewModel.openEmojiPicker() }) {
            Text("Сменить аватар")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Имя
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Имя",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayName.ifBlank { "Не указано" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                IconButton(onClick = { profileViewModel.startEditName() }) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать имя")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { authViewModel.signOut() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Выйти из аккаунта")
        }
    }

    // Диалог редактирования имени
    if (editingName) {
        AlertDialog(
            onDismissRequest = { profileViewModel.cancelEditName() },
            title = { Text("Введите имя") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { profileViewModel.updateNameInput(it) },
                    singleLine = true,
                    placeholder = { Text("Ваше имя") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { profileViewModel.saveName() }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { profileViewModel.cancelEditName() }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог выбора эмодзи (сетка 3x4)
    if (showEmojiPicker) {
        AlertDialog(
            onDismissRequest = { profileViewModel.closeEmojiPicker() },
            title = { Text("Выберите аватар") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(avatarEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (emoji == avatarEmoji)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (emoji == avatarEmoji) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { profileViewModel.selectEmoji(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 28.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { profileViewModel.closeEmojiPicker() }) {
                    Text("Закрыть")
                }
            }
        )
    }
}
