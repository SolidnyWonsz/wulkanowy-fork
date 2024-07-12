package io.github.wulkanowy.ui.base

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.wulkanowy.data.exceptions.NoCurrentStudentException
import io.github.wulkanowy.data.repositories.NoAuthorizationException
import io.github.wulkanowy.sdk.scrapper.exception.CloudflareVerificationException
import io.github.wulkanowy.sdk.scrapper.login.BadCredentialsException
import io.github.wulkanowy.sdk.scrapper.login.PasswordChangeRequiredException
import io.github.wulkanowy.utils.getErrorString
import io.github.wulkanowy.utils.security.ScramblerException
import timber.log.Timber
import javax.inject.Inject

open class ErrorHandler @Inject constructor(@ApplicationContext protected val context: Context) {

    var showErrorMessage: (String, Throwable) -> Unit = { _, _ -> }

    var onExpiredCredentials: () -> Unit = {}

    var onDecryptionFailed: () -> Unit = {}

    var onNoCurrentStudent: () -> Unit = {}

    var onPasswordChangeRequired: (String) -> Unit = {}

    var onAuthorizationRequired: () -> Unit = {}

    var onCaptchaVerificationRequired: (url: String?) -> Unit = {}

    fun dispatch(error: Throwable) {
        Timber.e(error, "An exception occurred while the Wulkanowy was running")
        proceed(error)
    }

    protected open fun proceed(error: Throwable) {
        showDefaultMessage(error)
        when (error) {
            is PasswordChangeRequiredException -> onPasswordChangeRequired(error.redirectUrl)
            is ScramblerException -> onDecryptionFailed()
            is BadCredentialsException -> onExpiredCredentials()
            is NoCurrentStudentException -> onNoCurrentStudent()
            is NoAuthorizationException -> onAuthorizationRequired()
            is CloudflareVerificationException -> onCaptchaVerificationRequired(error.originalUrl)
        }
    }

    fun showDefaultMessage(error: Throwable) {
        showErrorMessage(context.resources.getErrorString(error), error)
    }

    open fun clear() {
        showErrorMessage = { _, _ -> }
        onExpiredCredentials = {}
        onDecryptionFailed = {}
        onNoCurrentStudent = {}
        onPasswordChangeRequired = {}
        onAuthorizationRequired = {}
    }
}
