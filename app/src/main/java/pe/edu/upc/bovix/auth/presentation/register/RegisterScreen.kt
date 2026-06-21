package pe.edu.upc.bovix.auth.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.bovix.ui.theme.ForestGreen
import pe.edu.upc.bovix.ui.theme.MintGreen
import pe.edu.upc.bovix.ui.theme.PaleGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.registeredSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Cuenta creada!") },
            text = { Text("Tu cuenta fue creada exitosamente. Inicia sesión para continuar.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.consumeSuccess()
                    onRegisterSuccess()
                }) {
                    Text("Ir al login")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestGreen)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(vertical = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBackToLogin) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MintGreen, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text("Crear cuenta", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Únete a Bovix hoy", color = MintGreen, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(20.dp))

            Surface(
                color = Color.White.copy(alpha = 0.09f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    RegisterFieldLabel("Nombre completo")
                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        singleLine = true,
                        placeholder = { Text("Juan Quispe", color = PaleGreen.copy(alpha = 0.5f)) },
                        colors = registerDarkFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    RegisterFieldLabel("Correo electrónico")
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        singleLine = true,
                        placeholder = { Text("tu@correo.com", color = PaleGreen.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = registerDarkFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    RegisterFieldLabel("Contraseña")
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        placeholder = { Text("Mínimo 6 caracteres", color = PaleGreen.copy(alpha = 0.5f)) },
                        colors = registerDarkFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    RegisterFieldLabel("Confirmar contraseña")
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        placeholder = { Text("Repite tu contraseña", color = PaleGreen.copy(alpha = 0.5f)) },
                        colors = registerDarkFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.errorMessage?.let { msg ->
                        Spacer(Modifier.height(10.dp))
                        Text(msg, color = Color(0xFFFFB4B4), style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = viewModel::onRegisterClicked,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGreen,
                            contentColor = ForestGreen
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = ForestGreen, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Crear cuenta", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("¿Ya tienes cuenta? ", color = Color(0xFFA8D5BE), style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "Inicia sesión",
                            color = MintGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 2.dp).clickable { onBackToLogin() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RegisterFieldLabel(text: String) {
    Text(text, color = Color(0xFFA8D5BE), style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(6.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun registerDarkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = PaleGreen,
    unfocusedTextColor = PaleGreen,
    focusedContainerColor = Color.White.copy(alpha = 0.12f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
    focusedBorderColor = MintGreen,
    unfocusedBorderColor = Color.White.copy(alpha = 0.0f),
    cursorColor = MintGreen
)
