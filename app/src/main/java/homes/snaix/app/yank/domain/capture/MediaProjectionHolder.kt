package homes.snaix.app.yank.domain.capture

import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager

object MediaProjectionHolder {
    @Volatile var projection: MediaProjection? = null
        private set

    fun set(manager: MediaProjectionManager, resultCode: Int, data: Intent) {
        // Stop any prior projection so its system resources (virtual display token,
        // display server binder) are released; otherwise calling set() twice leaks.
        projection?.stop()
        projection = manager.getMediaProjection(resultCode, data)
        projection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { projection = null }
        }, null)
    }

    fun release() {
        projection?.stop()
        projection = null
    }

    fun isAvailable(): Boolean = projection != null
}
