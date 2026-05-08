package homes.snaix.app.yank

import android.app.Application

class YankApp : Application() {
    lateinit var di: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        di = AppContainer(this)
    }
}
