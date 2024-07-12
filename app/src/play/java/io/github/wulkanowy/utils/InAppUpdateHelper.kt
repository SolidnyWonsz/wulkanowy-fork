package io.github.wulkanowy.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus.DOWNLOADED
import com.google.android.play.core.install.model.InstallStatus.PENDING
import com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.updatePriority
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import io.github.wulkanowy.R
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class InAppUpdateHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsHelper: AnalyticsHelper,
    activity: Activity
) {

    lateinit var messageContainer: View

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(context) }

    private val flexibleUpdateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            PENDING -> Toast.makeText(context, R.string.update_download_started, Toast.LENGTH_SHORT)
                .show()

            DOWNLOADED -> popupSnackBarForCompleteUpdate()
            else -> Timber.d("Update state: ${state.installStatus()}")
        }
    }

    private inline val AppUpdateInfo.isImmediateUpdateAvailable: Boolean
        get() {
            val days = clientVersionStalenessDays() ?: 0
            val isUpdatePriorityAllowUpdate = when (updatePriority) {
                5 -> true
                4 -> days > 7
                3 -> days > 30
                else -> false
            }

            return updateAvailability() == UPDATE_AVAILABLE && isImmediateUpdateAllowed && isUpdatePriorityAllowUpdate
        }

    private inline val AppUpdateInfo.isFlexibleUpdateAvailable: Boolean
        get() {
            val days = clientVersionStalenessDays() ?: 0
            val isUpdatePriorityAllowUpdate = when (updatePriority) {
                4, 3, 2 -> true
                1 -> days >= 7
                0 -> false
                else -> false
            }

            return updateAvailability() == UPDATE_AVAILABLE && isFlexibleUpdateAllowed && isUpdatePriorityAllowUpdate
        }

    private val activityResultLauncher = (activity as AppCompatActivity).registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::onActivityResult
    )

    fun checkAndInstallUpdates() {
        Timber.d("Checking for updates...")
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.isImmediateUpdateAvailable -> {
                    startUpdate(appUpdateInfo, IMMEDIATE)
                }

                appUpdateInfo.isFlexibleUpdateAvailable -> {
                    appUpdateManager.registerListener(flexibleUpdateListener)
                    startUpdate(appUpdateInfo, FLEXIBLE)
                }

                else -> Timber.d("No update available")
            }
        }
    }

    private fun startUpdate(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        Timber.d("Start update ($updateType): $appUpdateInfo")

        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activityResultLauncher,
                AppUpdateOptions.defaultOptions(updateType)
            )
        } catch (e: Exception) {
            Timber.e(e, "Update failed!")
        }
    }

    private fun onActivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode

        if (resultCode != RESULT_OK) {
            Timber.i("Update failed! Result code: $resultCode")
            Toast.makeText(context, R.string.update_failed, Toast.LENGTH_LONG).show()
        }

        analyticsHelper.logEvent("inapp_update", "code" to resultCode)
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            Timber.d("InAppUpdate.onResume() listener: $info")

            when {
                DOWNLOADED == info.installStatus() -> popupSnackBarForCompleteUpdate()
                DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS == info.updateAvailability() -> {
                    startUpdate(
                        appUpdateInfo = info,
                        updateType = if (info.isImmediateUpdateAvailable) IMMEDIATE else FLEXIBLE
                    )
                }
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Timber.d("Show snackbar with update complete")
        Snackbar.make(
            messageContainer,
            R.string.update_download_success,
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(R.string.update_download_success_button) {
                appUpdateManager.completeUpdate()
                appUpdateManager.unregisterListener(flexibleUpdateListener)
            }
            show()
        }
    }
}
