package io.github.wulkanowy.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import fr.bipi.treessence.base.FormatterPriorityTree
import fr.bipi.treessence.common.StackTraceRecorder

class CrashLogTree : FormatterPriorityTree(Log.VERBOSE) {

    private val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (skipLog(priority, tag, message, t)) return

        crashlytics.log(format(priority, tag, message))
    }
}

class CrashLogExceptionTree : FormatterPriorityTree(Log.ERROR, ExceptionFilter) {

    private val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (skipLog(priority, tag, message, t)) return

        if (t != null) {
            crashlytics.recordException(t)
        } else {
            crashlytics.recordException(StackTraceRecorder(format(priority, tag, message)))
        }
    }
}
