package homes.snaix.app.yank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import homes.snaix.app.yank.ui.nav.YankNavGraph
import homes.snaix.app.yank.ui.theme.YankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { YankTheme { YankNavGraph() } }
    }
}
