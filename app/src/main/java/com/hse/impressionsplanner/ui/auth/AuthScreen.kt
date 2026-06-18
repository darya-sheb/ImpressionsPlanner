package com.hse.impressionsplanner.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun validateEmail(email: String): String? {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    if (!emailRegex.matches(email)) return "Некорректный email"
    return null
}

fun validatePassword(password: String): String? {
    if (password.length < 6) return "Пароль должен быть не менее 6 символов"
    if (!password.any { it.isUpperCase() }) return "Нужна хотя бы одна заглавная буква"
    if (!password.any { it.isLowerCase() }) return "Нужна хотя бы одна строчная буква"
    if (!password.any { it.isDigit() }) return "Нужна хотя бы одна цифра"
    return null
}

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Логотип
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🗺️", fontSize = 40.sp, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text       = "Планировщик\nвпечатлений",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text      = "Исследуй город, веди дневник",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Карточка формы
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(24.dp),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text       = if (isLoginMode) "Вход" else "Регистрация",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value         = email,
                    onValueChange = {
                        email      = it
                        emailError = null
                        viewModel.clearError()
                    },
                    label           = { Text("Email") },
                    isError         = emailError != null,
                    supportingText  = { emailError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier        = Modifier.fillMaxWidth(),
                    singleLine      = true,
                    shape           = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value         = password,
                    onValueChange = {
                        password      = it
                        passwordError = null
                        viewModel.clearError()
                    },
                    label                  = { Text("Пароль") },
                    isError                = passwordError != null,
                    supportingText         = { passwordError?.let { Text(it) } },
                    visualTransformation   = if (passwordVisible) VisualTransformation.None
                                             else PasswordVisualTransformation(),
                    keyboardOptions        = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon           = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                              else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Скрыть пароль"
                                                     else "Показать пароль"
                            )
                        }
                    },
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(12.dp)
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            emailError    = validateEmail(email)
                            passwordError = validatePassword(password)
                            if (emailError == null && passwordError == null) {
                                if (isLoginMode) viewModel.signIn(email, password)
                                else             viewModel.signUp(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text  = if (isLoginMode) "Войти" else "Зарегистрироваться",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            isLoginMode   = !isLoginMode
            emailError    = null
            passwordError = null
            viewModel.clearError()
        }) {
            Text(
                text  = if (isLoginMode) "Нет аккаунта? Зарегистрироваться"
                        else             "Уже есть аккаунт? Войти",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
