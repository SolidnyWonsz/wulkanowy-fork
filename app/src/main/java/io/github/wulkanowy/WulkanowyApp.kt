package io.github.wulkanowy

import android.app.Application
import android.util.Log.DEBUG
import android.util.Log.INFO
import android.util.Log.VERBOSE
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.HiltAndroidApp
import fr.bipi.treessence.file.FileLoggerTree
import io.github.wulkanowy.data.repositories.PreferencesRepository
import io.github.wulkanowy.ui.base.ThemeManager
import io.github.wulkanowy.utils.ActivityLifecycleLogger
import io.github.wulkanowy.utils.AnalyticsHelper
import io.github.wulkanowy.utils.AppInfo
import io.github.wulkanowy.utils.CrashLogExceptionTree
import io.github.wulkanowy.utils.CrashLogTree
import io.github.wulkanowy.utils.DebugLogTree
import io.github.wulkanowy.utils.RemoteConfigHelper
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WulkanowyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var appInfo: AppInfo

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var remoteConfigHelper: RemoteConfigHelper

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (appInfo.isDebug) VERBOSE else INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeAppLanguage()
        themeManager.applyDefaultTheme()
        remoteConfigHelper.initialize()
        initLogging()
    }

    private fun initLogging() {
        if (appInfo.isDebug) {
            Timber.plant(DebugLogTree())
            Timber.plant(
                FileLoggerTree.Builder()
                    .withFileName("wulkanowy.%g.log")
                    .withDirName(applicationContext.filesDir.absolutePath)
                    .withFileLimit(10)
                    .withMinPriority(DEBUG)
                    .build()
            )
        } else {
            Timber.plant(CrashLogExceptionTree())
            Timber.plant(CrashLogTree())
        }
        registerActivityLifecycleCallbacks(ActivityLifecycleLogger())
    }

    private fun initializeAppLanguage() {
        Lingver.init(this)

        if (preferencesRepository.appLanguage == "system") {
            Lingver.getInstance().setFollowSystemLocale(this)
            analyticsHelper.logEvent("language", "startup" to appInfo.systemLanguage)
        } else {
            analyticsHelper.logEvent("language", "startup" to preferencesRepository.appLanguage)
        }
    }
}
