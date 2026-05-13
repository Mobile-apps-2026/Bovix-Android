package pe.edu.upc.bovix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import pe.edu.upc.bovix.core.navigation.BovixNavGraph
import pe.edu.upc.bovix.ui.theme.BovixTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BovixTheme {
                BovixNavGraph()
            }
        }
    }
}
