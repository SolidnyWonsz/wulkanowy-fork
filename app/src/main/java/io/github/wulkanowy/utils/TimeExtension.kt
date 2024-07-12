package io.github.wulkanowy.utils

import java.text.SimpleDateFormat
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters.firstInMonth
import java.time.temporal.TemporalAdjusters.next
import java.time.temporal.TemporalAdjusters.previous
import java.util.Locale

private const val DEFAULT_DATE_PATTERN = "dd.MM.yyyy"

fun getDefaultLocaleWithFallback(): Locale {
    val locale = Locale.getDefault()
    if (locale.language == "csb") {
        return Locale.forLanguageTag("pl")
    }
    return locale
}

fun LocalDate.toTimestamp(): Long = atStartOfDay()
    .toInstant(ZoneOffset.UTC)
    .toEpochMilli()

fun Long.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(
    Instant.ofEpochMilli(this), ZoneOffset.UTC
)

fun Instant.toLocalDate(): LocalDate = atZone(ZoneOffset.UTC).toLocalDate()

fun String.toLocalDate(format: String = DEFAULT_DATE_PATTERN): LocalDate =
    LocalDate.parse(this, DateTimeFormatter.ofPattern(format))

fun LocalDate.toFormattedString(pattern: String = DEFAULT_DATE_PATTERN): String =
    format(DateTimeFormatter.ofPattern(pattern, getDefaultLocaleWithFallback()))

fun Instant.toFormattedString(
    pattern: String = DEFAULT_DATE_PATTERN,
    tz: ZoneId = ZoneId.systemDefault()
): String = atZone(tz).format(DateTimeFormatter.ofPattern(pattern))

fun Month.getFormattedName(): String {
    val formatter = SimpleDateFormat("LLLL", getDefaultLocaleWithFallback())

    val date = LocalDateTime.now().withMonth(value)
    return formatter.format(date.toInstant(ZoneOffset.UTC).toEpochMilli()).capitalise()
}

inline val LocalDate.nextSchoolDay: LocalDate
    get() {
        return when (dayOfWeek) {
            FRIDAY, SATURDAY, SUNDAY -> with(next(MONDAY))
            else -> plusDays(1)
        }
    }

inline val LocalDate.previousSchoolDay: LocalDate
    get() {
        return when (dayOfWeek) {
            SATURDAY, SUNDAY, MONDAY -> with(previous(FRIDAY))
            else -> minusDays(1)
        }
    }

inline val LocalDate.nextOrSameSchoolDay: LocalDate
    get() {
        return when (dayOfWeek) {
            SATURDAY, SUNDAY -> with(next(MONDAY))
            else -> this
        }
    }

inline val LocalDate.startExamsDay: LocalDate
    get() = nextOrSameSchoolDay.monday

inline val LocalDate.endExamsDay: LocalDate
    get() = nextOrSameSchoolDay.monday.plusWeeks(4).minusDays(1)

inline val LocalDate.previousOrSameSchoolDay: LocalDate
    get() {
        return when (dayOfWeek) {
            SATURDAY, SUNDAY -> with(previous(FRIDAY))
            else -> this
        }
    }

inline val LocalDate.weekDayName: String
    get() = format(DateTimeFormatter.ofPattern("EEEE", getDefaultLocaleWithFallback()))

inline val LocalDate.monday: LocalDate get() = with(MONDAY)

inline val LocalDate.sunday: LocalDate get() = with(SUNDAY)

/**
 * [Dz.U. 2016 poz. 1335](http://prawo.sejm.gov.pl/isap.nsf/DocDetails.xsp?id=WDU20160001335)
 */
val LocalDate.isHolidays: Boolean
    get() = isBefore(firstSchoolDayInCalendarYear) && isAfter(lastSchoolDayInCalendarYear)

val LocalDate.firstSchoolDayInSchoolYear: LocalDate
    get() = withYear(if (this.monthValue <= 6) this.year - 1 else this.year).firstSchoolDayInCalendarYear

val LocalDate.lastSchoolDayInSchoolYear: LocalDate
    get() = withYear(if (this.monthValue > 6) this.year + 1 else this.year).lastSchoolDayInCalendarYear

fun LocalDate.getLastSchoolDayIfHoliday(schoolYear: Int): LocalDate {
    val date = LocalDate.of(schoolYear.getSchoolYearByMonth(monthValue), monthValue, dayOfMonth)

    if (date.isHolidays) {
        return date.lastSchoolDayInCalendarYear
    }

    return date
}

private fun Int.getSchoolYearByMonth(monthValue: Int): Int {
    return when (monthValue) {
        in 9..12 -> this
        else -> this + 1
    }
}

private inline val LocalDate.firstSchoolDayInCalendarYear: LocalDate
    get() = LocalDate.of(year, 9, 1).run {
        when (dayOfWeek) {
            FRIDAY, SATURDAY, SUNDAY -> with(firstInMonth(MONDAY))
            else -> this
        }
    }

private inline val LocalDate.lastSchoolDayInCalendarYear: LocalDate
    get() = LocalDate.of(year, 6, 20)
        .with(next(FRIDAY))

