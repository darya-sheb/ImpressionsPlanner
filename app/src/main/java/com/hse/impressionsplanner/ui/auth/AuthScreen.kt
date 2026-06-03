package com.hse.impressionsplanner.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

fun validateEmail(email: String): String? {
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return "Некорректный email"
    }
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

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) "Вход" else "Регистрация",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
                viewModel.clearError()
            },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
                viewModel.clearError()
            },
            label = { Text("Пароль") },
            isError = passwordError != null,
            supportingText = { passwordError?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    emailError = validateEmail(email)
                    passwordError = validatePassword(password)
                    if (emailError == null && passwordError == null) {
                        if (isLoginMode) viewModel.signIn(email, password)
                        else viewModel.signUp(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoginMode) "Войти" else "Зарегистрироваться")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                isLoginMode = !isLoginMode
                emailError = null
                passwordError = null
                viewModel.clearError()
            }) {
                Text(if (isLoginMode) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти")
            }
        }
    }
}