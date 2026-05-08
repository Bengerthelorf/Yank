package homes.snaix.app.yank.trigger

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.domain.capture.CaptureService
import homes.snaix.app.yank.domain.capture.MediaProjectionHolder

class CaptureActivity : ComponentActivity() {

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val pm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            // Service must be foreground BEFORE we set the projection on API 34+
            startCaptureService()
            MediaProjectionHolder.set(pm, result.resultCode, result.data!!)
            triggerCapture()
        } else {
            Toast.makeText(this, "未授予录屏权限", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flow()
    }

    private fun flow() {
        if (isLocked()) {
            Toast.makeText(this, R.string.capture_locked, Toast.LENGTH_SHORT).show()
            finish(); return
        }
        val app = applicationContext as YankApp
        val keyConfigured = app.di.keyStore.getApiKey() != null
        if (!keyConfigured) {
            Toast.makeText(this, R.string.capture_no_api_key, Toast.LENGTH_SHORT).show()
            finish(); return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Soft check; main launcher requests POST_NOTIFICATIONS up front.
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }

        if (MediaProjectionHolder.isAvailable()) {
            startCaptureService()
            triggerCapture()
            finish()
        } else {
            val pm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(pm.createScreenCaptureIntent())
        }
    }

    private fun isLocked(): Boolean {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return km.isKeyguardLocked
    }

    private fun startCaptureService() {
        val intent = Intent(this, CaptureService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun triggerCapture() {
        val intent = Intent(this, CaptureService::class.java).apply { action = CaptureService.ACTION_CAPTURE }
        startService(intent)
    }
}
