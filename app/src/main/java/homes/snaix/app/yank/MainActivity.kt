package homes.snaix.app.yank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import homes.snaix.app.yank.ui.theme.YankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YankTheme {
                Scaffold { padding ->
                    Text("Yank · 上岛记", modifier = Modifier.padding(padding))
                }
            }
        }
    }
}
