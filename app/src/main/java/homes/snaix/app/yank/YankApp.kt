package homes.snaix.app.yank

import android.app.Application
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import homes.snaix.app.yank.trigger.CaptureActivity

class YankApp : Application() {
    lateinit var di: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        di = AppContainer(this)
        registerCaptureShortcut()
    }

    private fun registerCaptureShortcut() {
        val intent = Intent(this, CaptureActivity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val shortcut = ShortcutInfoCompat.Builder(this, "yank_capture")
            .setShortLabel(getString(R.string.shortcut_capture_short))
            .setLongLabel(getString(R.string.shortcut_capture_long))
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_yank))
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
    }
}
