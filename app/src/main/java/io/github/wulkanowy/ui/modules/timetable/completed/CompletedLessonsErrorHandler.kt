package io.github.wulkanowy.ui.modules.timetable.completed

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.wulkanowy.sdk.scrapper.exception.FeatureDisabledException
import io.github.wulkanowy.ui.base.ErrorHandler
import javax.inject.Inject

class CompletedLessonsErrorHandler @Inject constructor(@ApplicationContext context: Context) :
    ErrorHandler(context) {

    var onFeatureDisabled: () -> Unit = {}

    override fun proceed(error: Throwable) {
        when (error) {
            is FeatureDisabledException -> onFeatureDisabled()
            else -> super.proceed(error)
        }
    }

    override fun clear() {
        super.clear()
        onFeatureDisabled = {}
    }
}
