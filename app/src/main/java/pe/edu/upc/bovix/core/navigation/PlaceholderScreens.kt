package pe.edu.upc.bovix.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pe.edu.upc.bovix.ui.theme.BgPrimary
import pe.edu.upc.bovix.ui.theme.TextMute

@Composable
fun ComingSoonScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$name — próximamente",
            style = MaterialTheme.typography.titleMedium,
            color = TextMute
        )
    }
}
