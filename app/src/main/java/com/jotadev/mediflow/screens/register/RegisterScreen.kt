package com.jotadev.mediflow.screens.register

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
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
import android.util.Log
import com.jotadev.mediflow.R

data class RegisterState(
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val rol: String = "",
    val dni: String = "",
    val area: String = "",
    val cargo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = ""
)

val ROLES_OPCIONES = listOf(
    "Asistencial",
    "Administrativo",
    "Técnico",
    "Profesional de salud",
    "Directivo",
    "Operativo",
    "Apoyo diagnóstico",
    "Apoyo terapéutico",
    "Servicios generales",
    "Salud ocupacional",
    "Seguridad y salud en el trabajo",
    "Educador en salud",
    "Investigador",
    "Coordinador",
    "Supervisor"
)

val AREAS_OPCIONES = listOf(
    "Medicina general",
    "Medicina especializada",
    "Enfermería",
    "Obstetricia",
    "Psicología",
    "Odontología",
    "Nutrición",
    "Terapia física y rehabilitación",
    "Laboratorio clínico",
    "Radiología e imagenología",
    "Farmacia",
    "Emergencias y urgencias",
    "Hospitalización",
    "Centro quirúrgico",
    "Centro obstétrico",
    "Banco de sangre",
    "Servicio de imágenes",
    "Farmacotecnia",
    "Rehabilitación física",
    "Dirección médica",
    "Dirección administrativa",
    "Recursos humanos",
    "Logística y almacén",
    "Contabilidad y finanzas",
    "Estadística e informática",
    "Archivo clínico",
    "Atención al usuario",
    "Mantenimiento",
    "Limpieza y desinfección",
    "Seguridad y vigilancia",
    "Alimentación y cocina",
    "Lavandería"
)

val CARGOS_OPCIONES = listOf(
    "Médico general",
    "Médico especialista",
    "Enfermero/a",
    "Obstetra",
    "Psicólogo/a",
    "Odontólogo/a",
    "Nutricionista",
    "Técnico de laboratorio",
    "Técnico en radiología",
    "Químico farmacéutico",
    "Técnico en farmacia",
    "Cirujano",
    "Anestesiólogo",
    "Personal de limpieza",
    "Personal de vigilancia",
    "Cocinero/a",
    "Lavandero/a",
    "Administrador",
    "Contador",
    "Estadístico",
    "Archivador clínico",
    "Recepcionista",
    "Jefe de recursos humanos",
    "Coordinador de salud ocupacional",
    "Inspector de SST",
    "Ergónomo/a",
    "Higienista industrial",
    "Técnico en seguridad laboral",
    "Miembro del comité SST",
    "Director médico",
    "Director administrativo",
    "Educador en salud",
    "Promotor de salud",
    "Investigador clínico"
)

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory)
    val uiState = registerViewModel.uiState.collectAsState().value

    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterUiState.Success -> {
                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                onLoginClick()
            }
            is RegisterUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    val state = remember { mutableStateOf(RegisterState()) }
    val mostrarContrasena = remember { mutableStateOf(false) }
    val mostrarConfirmarContrasena = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimary)
    ) {
        // Fondo con onda azul
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.4f)
                quadraticTo(
                    size.width * 0.5f, size.height * 0.55f,
                    0f, size.height * 0.4f
                )
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFF0081D4)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
//            Spacer(modifier = Modifier.height(30.dp))
            LogoMediFlow()
            Spacer(modifier = Modifier.height(8.dp))
            CardRegistro(
                state = state.value,
                onStateChange = { newState -> state.value = newState },
                mostrarContrasena = mostrarContrasena.value,
                mostrarConfirmarContrasena = mostrarConfirmarContrasena.value,
                onToggleContrasena = { mostrarContrasena.value = !mostrarContrasena.value },
                onToggleConfirmarContrasena = { mostrarConfirmarContrasena.value = !mostrarConfirmarContrasena.value },
                onRegistrarClick = {
                    if (validarFormulario(state.value)) {
                        Log.d("RegisterScreen", "Nombre: '${state.value.nombre}'")
                        Log.d("RegisterScreen", "Apellido: '${state.value.apellido}'")
                        val displayName = "${state.value.nombre} ${state.value.apellido}".trim()
                        Log.d("RegisterScreen", "DisplayName: '$displayName'")
                        registerViewModel.registerWithEmail(
                            email = state.value.correo,
                            password = state.value.contrasena,
                            name = displayName,
                            rol = state.value.rol,
                            dni = state.value.dni,
                            area = state.value.area,
                            cargo = state.value.cargo
                        )
                    }
                },
                onLoginClick = onLoginClick
            )
        }
    }
}

@Composable
private fun LogoMediFlow() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Transparent)
    ) {
        Image(
            painter = painterResource(id = R.drawable.mediflow),
            contentDescription = "Logo MediFlow",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Composable
fun CardRegistro(
    state: RegisterState,
    onStateChange: (RegisterState) -> Unit,
    mostrarContrasena: Boolean,
    mostrarConfirmarContrasena: Boolean,
    onToggleContrasena: () -> Unit,
    onToggleConfirmarContrasena: () -> Unit,
    onRegistrarClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Regístrate",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Llena el formulario para crear una cuenta",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campos del formulario
            CamposRegistro(
                state = state,
                onStateChange = onStateChange,
                mostrarContrasena = mostrarContrasena,
                mostrarConfirmarContrasena = mostrarConfirmarContrasena,
                onToggleContrasena = onToggleContrasena,
                onToggleConfirmarContrasena = onToggleConfirmarContrasena
            )

            Spacer(modifier = Modifier.height(16.dp))

            BotonRegistro(
                onClick = onRegistrarClick,
                enabled = validarFormulario(state)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextoInicioSesion(onLoginClick = onLoginClick)
        }
    }
}
@Composable
fun CamposRegistro(
    state: RegisterState,
    onStateChange: (RegisterState) -> Unit,
    mostrarContrasena: Boolean,
    mostrarConfirmarContrasena: Boolean,
    onToggleContrasena: () -> Unit,
    onToggleConfirmarContrasena: () -> Unit
) {
    Column {
        // Campo Nombre con error dinámico
        val nombreError = if (state.nombre.isNotEmpty() && !isValidName(state.nombre)) {
            "Nombre inválido: solo letras, mínimo 2 caracteres"
        } else null
        Column {
            CampoTextoReutilizable(
                valor = state.nombre,
                onValorCambiado = { onStateChange(state.copy(nombre = it)) },
                label = "Nombre",
                icono = Icons.Default.Person
            )
            AnimatedVisibility(
                visible = nombreError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (nombreError != null) {
                    Text(
                        text = nombreError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Apellidos con error dinámico
        val apellidoError = if (state.apellido.isNotEmpty() && !isValidName(state.apellido)) {
            "Apellidos inválidos: solo letras, mínimo 2 caracteres"
        } else null
        Column {
            CampoTextoReutilizable(
                valor = state.apellido,
                onValorCambiado = { onStateChange(state.copy(apellido = it)) },
                label = "Apellidos",
                icono = Icons.Default.Person2
            )
            AnimatedVisibility(
                visible = apellidoError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (apellidoError != null) {
                    Text(
                        text = apellidoError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Correo con error dinámico
        val correoError = if (state.correo.isNotEmpty() && !isValidEmail(state.correo)) {
            "Correo inválido"
        } else null
        Column {
            CampoTextoReutilizable(
                valor = state.correo,
                onValorCambiado = { onStateChange(state.copy(correo = it)) },
                label = "Correo",
                icono = Icons.Default.Email,
                tipoTeclado = KeyboardType.Email
            )
            AnimatedVisibility(
                visible = correoError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (correoError != null) {
                    Text(
                        text = correoError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Campo DNI con error dinámico
        val dniError = if (state.dni.isNotEmpty() && state.dni.length != 8) {
            "DNI debe tener 8 dígitos"
        } else null
        Column {
            CampoTextoReutilizable(
                valor = state.dni,
                onValorCambiado = { onStateChange(state.copy(dni = it)) },
                label = "DNI",
                icono = Icons.Default.CardGiftcard,
                tipoTeclado = KeyboardType.Number,
                maxCaracteres = 8
            )
            AnimatedVisibility(
                visible = dniError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (dniError != null) {
                    Text(
                        text = dniError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Rol (lista estática)
        Column {
            CampoDropdownReutilizable(
                valor = state.rol,
                onValorCambiado = { onStateChange(state.copy(rol = it)) },
                label = "Rol",
                icono = Icons.Default.Work,
                opciones = ROLES_OPCIONES
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Campo Área (lista estática)
        Column {
            CampoDropdownReutilizable(
                valor = state.area,
                onValorCambiado = { onStateChange(state.copy(area = it)) },
                label = "Área",
                icono = Icons.Default.WorkOutline,
                opciones = AREAS_OPCIONES
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Cargo (lista estática)
        Column {
            CampoDropdownReutilizable(
                valor = state.cargo,
                onValorCambiado = { onStateChange(state.copy(cargo = it)) },
                label = "Cargo",
                icono = Icons.Default.Work,
                opciones = CARGOS_OPCIONES
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Campo Contraseña con error dinámico
        val contrasenaError = if (state.contrasena.isNotEmpty() && !isStrongPassword(state.contrasena)) {
            "Contraseña débil: mín 8, mayús, minús, número y símbolo"
        } else null
        Column {
            CampoTextoReutilizable(
                valor = state.contrasena,
                onValorCambiado = { onStateChange(state.copy(contrasena = it)) },
                label = "Contraseña",
                icono = Icons.Default.Lock,
                tipoTeclado = KeyboardType.Password,
                esContrasena = true,
                mostrarContrasena = mostrarContrasena,
                onToggleVisibilidad = onToggleContrasena
            )
            AnimatedVisibility(
                visible = contrasenaError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (contrasenaError != null) {
                    Text(
                        text = contrasenaError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Confirmar Contraseña con error dinámico
        val confirmarError = if (state.confirmarContrasena.isNotEmpty() && state.contrasena != state.confirmarContrasena) {
            "No coincide con la contraseña"
        } else null
        Column {
            CampoTextoReutilizable(
                valor = state.confirmarContrasena,
                onValorCambiado = { onStateChange(state.copy(confirmarContrasena = it)) },
                label = "Confirmar Contraseña",
                icono = Icons.Default.Lock,
                tipoTeclado = KeyboardType.Password,
                esContrasena = true,
                mostrarContrasena = mostrarConfirmarContrasena,
                onToggleVisibilidad = onToggleConfirmarContrasena
            )
            AnimatedVisibility(
                visible = confirmarError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (confirmarError != null) {
                    Text(
                        text = confirmarError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CampoTextoReutilizable(
    valor: String,
    onValorCambiado: (String) -> Unit,
    label: String,
    icono: ImageVector,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    esContrasena: Boolean = false,
    mostrarContrasena: Boolean = false,
    onToggleVisibilidad: (() -> Unit)? = null,
    maxCaracteres: Int = Int.MAX_VALUE
) {
    val valorFiltrado = if (tipoTeclado == KeyboardType.Number) {
        valor.filter { it.isDigit() }
    } else {
        valor
    }.take(maxCaracteres)

    OutlinedTextField(
        value = valorFiltrado,
        onValueChange = onValorCambiado,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icono,
                contentDescription = "Icono de $label",
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
        visualTransformation = if (esContrasena && !mostrarContrasena)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        trailingIcon = {
            if (esContrasena && onToggleVisibilidad != null) {
                IconButton(onClick = onToggleVisibilidad) {
                    Icon(
                        imageVector = if (mostrarContrasena)
                            Icons.Default.VisibilityOff
                        else
                            Icons.Default.Visibility,
                        contentDescription = if (mostrarContrasena)
                            "Ocultar contraseña"
                        else
                            "Mostrar contraseña",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoDropdownReutilizable(
    valor: String,
    onValorCambiado: (String) -> Unit,
    label: String,
    icono: ImageVector,
    opciones: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icono,
                    contentDescription = "Icono de $label",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
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
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme .colorScheme.secondary.copy(alpha = 0.5f)
        ) {
            opciones.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValorCambiado(option)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}

@Composable
fun BotonRegistro(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(100),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = Color.Gray
        ),
        enabled = enabled
    ) {
        Text(
            text = "Registrarse",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun TextoInicioSesion(onLoginClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "¿Ya tienes una cuenta? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondary,
        )
        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.clickable { onLoginClick() }
        )
    }
}

// Función de validación del formulario
private fun validarFormulario(state: RegisterState): Boolean {
    return isValidName(state.nombre) &&
            isValidName(state.apellido) &&
            isValidEmail(state.correo) &&
            state.dni.length == 8 &&
            state.area.length >= 2 &&
            state.cargo.length >= 2 &&
            isStrongPassword(state.contrasena) &&
            state.contrasena == state.confirmarContrasena
}
// Validar email
private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// Validar nombre/apellido (solo letras y espacios, al menos 2)
private fun isValidName(name: String): Boolean {
    val trimmed = name.trim()
    val regex = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]{2,}$".toRegex()
    return regex.matches(trimmed)
}

// Validar contraseña fuerte: 8+, mayúscula, minúscula, dígito y símbolo
private fun isStrongPassword(password: String): Boolean {
    if (password.length < 8) return false
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val hasSpace = password.any { it.isWhitespace() }
    return hasUpper && hasLower && hasDigit && hasSpecial && !hasSpace
}