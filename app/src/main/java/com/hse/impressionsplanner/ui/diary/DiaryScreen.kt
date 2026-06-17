package com.hse.impressionsplanner.ui.diary

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hse.impressionsplanner.data.DiaryEntry
import com.hse.impressionsplanner.data.repository.StructuredEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: DiaryViewModel = viewModel()) {
    val entries      by viewModel.entries.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val showDialog   by viewModel.showDialog.collectAsState()
    val inputText    by viewModel.inputText.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val previewEntry by viewModel.previewEntry.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val speechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull() ?: ""
            if (spoken.isNotBlank()) {
                viewModel.updateInput(
                    if (inputText.isBlank()) spoken else "$inputText $spoken"
                )
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick  = { viewModel.openDialog() },
                icon     = { Icon(Icons.Default.Add, contentDescription = null) },
                text     = { Text("Новая запись") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Дневник впечатлений",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Записей пока нет",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нажмите «+» чтобы добавить впечатление",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        DiaryEntryCard(
                            entry    = entry,
                            onDelete = { viewModel.deleteEntry(entry.id) }
                        )
                    }
                }
            }
        }
    }

    // Диалог: ввод текста
    if (showDialog && previewEntry == null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDialog() },
            title = { Text("Новое впечатление") },
            text = {
                Column {
                    OutlinedTextField(
                        value       = inputText,
                        onValueChange = { viewModel.updateInput(it) },
                        placeholder = { Text("Опишите ваше впечатление...") },
                        minLines    = 4,
                        maxLines    = 8,
                        modifier    = Modifier.fillMaxWidth(),
                        enabled     = !isProcessing
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        if (speechAvailable) {
                            OutlinedButton(
                                onClick  = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите...")
                                    }
                                    speechLauncher.launch(intent)
                                },
                                enabled  = !isProcessing
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Голосовой ввод",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Голос")
                            }
                        } else {
                            Text(
                                text  = "Голосовой ввод недоступен",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    // Ошибка GigaChat
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.processWithGigaChat() },
                    enabled  = inputText.isNotBlank() && !isProcessing
                ) {
                    Text("Создать впечатление")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick  = { viewModel.saveEntryRaw() },
                        enabled  = inputText.isNotBlank() && !isProcessing
                    ) {
                        Text("Сохранить как есть")
                    }
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("Отмена")
                    }
                }
            }
        )
    }

    // Диалог: превью структурированной записи
    if (showDialog && previewEntry != null) {
        AlertDialog(
            onDismissRequest = { viewModel.discardPreview() },
            title = { Text("Впечатление готово") },
            text  = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    PreviewEntry(previewEntry!!)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmPreview() }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.discardPreview() }) {
                    Text("Изменить")
                }
            }
        )
    }
}

@Composable
private fun PreviewEntry(entry: StructuredEntry) {
    @Composable
    fun Field(label: String, value: String) {
        if (value.isNotBlank()) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text  = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    Field("Место", entry.place)
    Field("Продолжительность", entry.duration)
    Field("Компания", entry.company)
    Field("Погода", entry.weather)
    Field("Еда", entry.food)
    Field("Сюрприз", entry.surprise)
    Field("Эмоции", entry.emotions)
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    Text(
        text      = "Итог",
        style     = MaterialTheme.typography.labelSmall,
        color     = MaterialTheme.colorScheme.primary
    )
    Text(
        text  = entry.summary,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    onDelete: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (entry.place.isNotBlank()) {
                        Text(
                            text       = entry.place,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text  = entry.formattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Метаданные (погода, компания)
            val meta = listOfNotNull(
                entry.weather.takeIf { it.isNotBlank() },
                entry.company.takeIf { it.isNotBlank() },
                entry.duration.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text  = entry.summary.ifBlank { entry.rawText },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (entry.emotions.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = entry.emotions,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
