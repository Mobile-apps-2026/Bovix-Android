package pe.edu.upc.bovix.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.bovix.ui.theme.ForestGreen
import pe.edu.upc.bovix.ui.theme.MintGreen
import pe.edu.upc.bovix.ui.theme.PaleGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedUser) {
        if (state.loggedUser != null) {
            onLoginSuccess()
            viewModel.consumeNavigation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestGreen)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Ícono de la app
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MintGreen, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Bovix",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontSize = 32.sp
            )
            Text(
                text = "Tu ganado en control",
                color = MintGreen,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            // Formulario de login
            Surface(
                color = Color.White.copy(alpha = 0.09f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Correo electrónico",
                        color = Color(0xFFA8D5BE),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        singleLine = true,
                        placeholder = { Text("juan@ejemplo.com", color = PaleGreen.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = bovixDarkFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Contraseña",
                        color = Color(0xFFA8D5BE),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        placeholder = { Text("••••••••", color = PaleGreen.copy(alpha = 0.5f)) },
                        colors = bovixDarkFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.errorMessage?.let { msg ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = msg,
                            color = Color(0xFFFFB4B4),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = viewModel::onLoginClicked,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGreen,
                            contentColor = ForestGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = ForestGreen,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Iniciar sesión", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "¿No tienes cuenta? ",
                            color = Color(0xFFA8D5BE),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Regístrate",
                            color = MintGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.15f))
                Text(
                    text = "  o continúa con  ",
                    color = Color(0xFFA8D5BE),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.15f))
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Text("Continuar con Google")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun bovixDarkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = PaleGreen,
    unfocusedTextColor = PaleGreen,
    focusedContainerColor = Color.White.copy(alpha = 0.12f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
    focusedBorderColor = MintGreen,
    unfocusedBorderColor = Color.White.copy(alpha = 0.0f),
    cursorColor = MintGreen
)
