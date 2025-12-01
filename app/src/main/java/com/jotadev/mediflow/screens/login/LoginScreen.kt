@file:Suppress("DEPRECATION")

package com.jotadev.mediflow.screens.login

import android.app.Activity
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import android.widget.Toast
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.google.android.gms.common.api.ApiException
import com.jotadev.mediflow.R


@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit, onLoginClick: () -> Unit
) {
    val usuario = remember { mutableStateOf("") }
    val contrasena = remember { mutableStateOf("") }
    val mostrarContrasena = remember { mutableStateOf(false) }
    val emailTouched = remember { mutableStateOf(false) }
    val pwdTouched = remember { mutableStateOf(false) }

    // Configuración de Google Sign-In + Firebase
    val context = LocalContext.current
    val gso =
        Builder(DEFAULT_SIGN_IN).requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail().build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    val viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("Login", "GoogleSignIn resultCode=${result.resultCode}, hasData=${result.data != null}")
            val intentData = result.data
            if (intentData != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(intentData)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d("Login", "Google account: ${account.email}")
                    val idToken = account.idToken
                    Log.d("Login", "Google idToken is null? ${idToken == null}")
                    if (idToken != null) {
                        viewModel.loginWithGoogle(idToken)
                    } else {
                        Toast.makeText(context, "No se obtuvo token de Google", Toast.LENGTH_LONG).show()
                    }
                } catch (e: ApiException) {
                    Log.e("Login", "Google sign in failed: ${e.message}")
                    Toast.makeText(context, "Google Sign-In falló: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    // Mostrar toasts y navegar cuando el login sea exitoso
    val state = viewModel.uiState.collectAsState().value
    LaunchedEffect(state) {
        when (state) {
            is LoginUiState.Success -> {
                Toast.makeText(context, "Bienvenido", Toast.LENGTH_SHORT).show()
                onLoginClick()
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.onPrimary)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.4f)
                quadraticTo(
                    size.width * 0.5f, size.height * 0.55f, 0f, size.height * 0.4f
                )
                close()
            }
            drawPath(
                path = path, color = Color(0xFF0081D4)
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            LogoMediFlow()
            Spacer(modifier = Modifier.height(16.dp))
            NotificationsPermissionPrompt()
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(
                    topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Inicia sesión para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    // Validaciones progresivas: se muestran tras interacción
                    val emailText = usuario.value.trim()
                    val emailError: String? = if (emailTouched.value) {
                        when {
                            emailText.isEmpty() -> "El correo es obligatorio"
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailText)
                                .matches() -> "Correo inválido"

                            else -> null
                        }
                    } else null
                    val isEmailValid = emailError == null

                    val passwordText = contrasena.value
                    val isPwdStrong = isStrongPassword(passwordText)
                    val passwordError: String? = if (pwdTouched.value) {
                        when {
                            passwordText.isEmpty() -> "La contraseña es obligatoria"
                            !isPwdStrong -> "Debe tener 8+ caracteres, mayúscula, minúscula, dígito y símbolo; sin espacios"
                            else -> null
                        }
                    } else null
                    // Campo de correo con error dinámico
                    Column {
                        CampoTextoUsuario(
                            valor = usuario.value,
                            onValorCambiado = { emailTouched.value = true; usuario.value = it })
                        AnimatedVisibility(
                            visible = emailError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (emailError != null) {
                                Text(
                                    text = emailError,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Campo de contraseña con error dinámico
                    Column {
                        CampoTextoContrasena(
                            valor = contrasena.value,
                            onValorCambiado = { pwdTouched.value = true; contrasena.value = it },
                            mostrarContrasena = mostrarContrasena.value,
                            onToggleVisibilidad = {
                                mostrarContrasena.value = !mostrarContrasena.value
                            })
                        AnimatedVisibility(
                            visible = passwordError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (passwordError != null) {
                                Text(
                                    text = passwordError,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = false,
                                onCheckedChange = {},
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.secondary,
                                    uncheckedColor = MaterialTheme.colorScheme.secondary
                                )
                            )
                            Text(
                                text = "Recordar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.clickable {
                                // Sin funcionalidad
                            })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    BotonIngresar(
                        onClick = {
                            val email = usuario.value.trim()
                            val password = contrasena.value
                            if (isEmailValid && isPwdStrong) {
                                viewModel.loginWithEmail(email, password)
                            }
                        }, enabled = isEmailValid && isPwdStrong
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f).height(1.dp)
                        )
                        Text(
                            text = "O ingresa con",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp,
                            modifier = Modifier.weight(1f).height(1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    GoogleButton(
                        onClick = {
                            val intent = googleSignInClient.signInIntent
                            launcher.launch(intent)
                        })
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "¿No tienes una cuenta? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                        Text(
                            text = "Regístrate",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.clickable { onRegisterClick() })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsPermissionPrompt() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Las notificaciones están desactivadas. Puedes habilitarlas en Ajustes.", Toast.LENGTH_LONG).show()
            }
        }
        if (!granted) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Permitir notificaciones",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Te avisaremos cuando se asigne o actualice tu horario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { launcher.launch(permission) }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )) {
                        Text("Permitir notificaciones")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LogoMediFlow() {
    Box(
        modifier = Modifier.size(200.dp).background(Color.Transparent)
    ) {
        Image(
            painter = painterResource(id = R.drawable.mediflow),
            contentDescription = "Logo MediFlow",
            modifier = Modifier.fillMaxSize().padding(16.dp)
        )
    }
}

@Composable
private fun CampoTextoUsuario(
    valor: String, onValorCambiado: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambiado,
        label = { Text("Correo") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Icono de email",
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(100),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}

@Composable
private fun CampoTextoContrasena(
    valor: String,
    onValorCambiado: (String) -> Unit,
    mostrarContrasena: Boolean,
    onToggleVisibilidad: () -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambiado,
        label = { Text("Contraseña") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Icono de contraseña",
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(100),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
        ),
        visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibilidad) {
                Icon(
                    imageVector = if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
private fun BotonIngresar(
    onClick: () -> Unit, enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(100),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = Color.Gray
        ),
        enabled = enabled
    ) {
        Text(
            text = "Ingresar", style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold, fontSize = 18.sp
            ), color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// Validación de contraseña para Login (consistente con registro)
private fun isStrongPassword(password: String): Boolean {
    if (password.length < 8) return false
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val hasSpace = password.any { it.isWhitespace() }
    return hasUpper && hasLower && hasDigit && hasSpecial && !hasSpace
}

@Composable
private fun GoogleButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(100)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = "Icono de Google",
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Google",
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold, fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}