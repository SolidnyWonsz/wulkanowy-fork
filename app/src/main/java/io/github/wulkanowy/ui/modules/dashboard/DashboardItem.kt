package io.github.wulkanowy.ui.modules.dashboard

import io.github.wulkanowy.data.db.entities.*
import io.github.wulkanowy.data.enums.GradeColorTheme
import io.github.wulkanowy.data.pojos.TimetableFull
import io.github.wulkanowy.utils.AdBanner
import io.github.wulkanowy.data.db.entities.Homework as EntitiesHomework

sealed class DashboardItem(val type: Type) {

    abstract val error: Throwable?

    abstract val isLoading: Boolean

    abstract val isDataLoaded: Boolean

    data class AdminMessages(
        val adminMessage: AdminMessage? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.ADMIN_MESSAGE) {

        override val isDataLoaded get() = adminMessage != null
    }

    data class Account(
        val student: Student? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.ACCOUNT) {

        override val isDataLoaded get() = student != null
    }

    data class HorizontalGroup(
        val unreadMessagesCount: Cell<Int?>? = null,
        val attendancePercentage: Cell<Double>? = null,
        val luckyNumber: Cell<Int>? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.HORIZONTAL_GROUP) {

        data class Cell<T>(
            val data: T?,
            val error: Boolean,
            val isLoading: Boolean,
        ) {
            val isHidden: Boolean
                get() = data == null && !error && !isLoading
        }

        override val isDataLoaded
            get() = unreadMessagesCount?.isLoading == false || attendancePercentage?.isLoading == false || luckyNumber?.isLoading == false

        val isFullDataLoaded
            get() = luckyNumber?.isLoading != true && attendancePercentage?.isLoading != true && unreadMessagesCount?.isLoading != true
    }

    data class Grades(
        val subjectWithGrades: Map<String, List<Grade>>? = null,
        val gradeTheme: GradeColorTheme? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.GRADES) {

        override val isDataLoaded get() = subjectWithGrades != null
    }

    data class Lessons(
        val lessons: TimetableFull? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.LESSONS) {

        override val isDataLoaded get() = lessons != null
    }

    data class Homework(
        val homework: List<EntitiesHomework>? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.HOMEWORK) {

        override val isDataLoaded get() = homework != null
    }

    data class Announcements(
        val announcement: List<SchoolAnnouncement>? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.ANNOUNCEMENTS) {

        override val isDataLoaded get() = announcement != null
    }

    data class Exams(
        val exams: List<Exam>? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.EXAMS) {

        override val isDataLoaded get() = exams != null
    }

    data class Conferences(
        val conferences: List<Conference>? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.CONFERENCES) {

        override val isDataLoaded get() = conferences != null
    }

    data class Ads(
        val adBanner: AdBanner? = null,
        override val error: Throwable? = null,
        override val isLoading: Boolean = false
    ) : DashboardItem(Type.ADS) {

        override val isDataLoaded get() = adBanner != null
    }

    enum class Type {
        ADMIN_MESSAGE,
        ACCOUNT,
        HORIZONTAL_GROUP,
        LESSONS,
        ADS,
        GRADES,
        HOMEWORK,
        ANNOUNCEMENTS,
        EXAMS,
        CONFERENCES,
    }

    enum class Tile {
        ADMIN_MESSAGE,
        ACCOUNT,
        LUCKY_NUMBER,
        MESSAGES,
        ATTENDANCE,
        LESSONS,
        ADS,
        GRADES,
        HOMEWORK,
        ANNOUNCEMENTS,
        EXAMS,
        CONFERENCES,
    }
}

fun DashboardItem.Tile.toDashboardItemType() = when (this) {
    DashboardItem.Tile.ADMIN_MESSAGE -> DashboardItem.Type.ADMIN_MESSAGE
    DashboardItem.Tile.ACCOUNT -> DashboardItem.Type.ACCOUNT
    DashboardItem.Tile.LUCKY_NUMBER -> DashboardItem.Type.HORIZONTAL_GROUP
    DashboardItem.Tile.MESSAGES -> DashboardItem.Type.HORIZONTAL_GROUP
    DashboardItem.Tile.ATTENDANCE -> DashboardItem.Type.HORIZONTAL_GROUP
    DashboardItem.Tile.LESSONS -> DashboardItem.Type.LESSONS
    DashboardItem.Tile.GRADES -> DashboardItem.Type.GRADES
    DashboardItem.Tile.HOMEWORK -> DashboardItem.Type.HOMEWORK
    DashboardItem.Tile.ANNOUNCEMENTS -> DashboardItem.Type.ANNOUNCEMENTS
    DashboardItem.Tile.EXAMS -> DashboardItem.Type.EXAMS
    DashboardItem.Tile.CONFERENCES -> DashboardItem.Type.CONFERENCES
    DashboardItem.Tile.ADS -> DashboardItem.Type.ADS
}
