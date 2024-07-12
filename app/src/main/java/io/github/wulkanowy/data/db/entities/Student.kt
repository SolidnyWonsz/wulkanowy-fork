package io.github.wulkanowy.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.Instant

@Entity(
    tableName = "Students",
    indices = [Index(
        value = ["email", "symbol", "student_id", "school_id", "class_id"],
        unique = true
    )]
)
data class Student(

    @ColumnInfo(name = "scrapper_base_url")
    val scrapperBaseUrl: String,

    @ColumnInfo(name = "scrapper_domain_suffix", defaultValue = "")
    val scrapperDomainSuffix: String,

    @ColumnInfo(name = "mobile_base_url")
    val mobileBaseUrl: String,

    @ColumnInfo(name = "login_type")
    val loginType: String,

    @ColumnInfo(name = "login_mode")
    val loginMode: String,

    @ColumnInfo(name = "certificate_key")
    val certificateKey: String,

    @ColumnInfo(name = "private_key")
    val privateKey: String,

    @ColumnInfo(name = "is_parent")
    val isParent: Boolean,

    val email: String,

    var password: String,

    val symbol: String,

    @ColumnInfo(name = "student_id")
    val studentId: Int,

    @Deprecated("not available in VULCAN anymore")
    @ColumnInfo(name = "user_login_id")
    val userLoginId: Int,

    @ColumnInfo(name = "user_name")
    val userName: String,

    @ColumnInfo(name = "student_name")
    val studentName: String,

    @ColumnInfo(name = "school_id")
    val schoolSymbol: String,

    @ColumnInfo(name = "school_short")
    val schoolShortName: String,

    @ColumnInfo(name = "school_name")
    val schoolName: String,

    @ColumnInfo(name = "class_name")
    val className: String,

    @ColumnInfo(name = "class_id")
    val classId: Int,

    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean,

    @ColumnInfo(name = "registration_date")
    val registrationDate: Instant,

    @ColumnInfo(name = "is_authorized", defaultValue = "0")
    val isAuthorized: Boolean,

    @ColumnInfo(name = "is_edu_one", defaultValue = "NULL")
    val isEduOne: Boolean?,

) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var nick = ""

    @ColumnInfo(name = "avatar_color")
    var avatarColor = 0L
}

@Entity
data class StudentIsAuthorized(

    @PrimaryKey
    var id: Long,

    @ColumnInfo(name = "is_authorized", defaultValue = "NULL")
    val isAuthorized: Boolean?,
) : Serializable

@Entity
data class StudentIsEduOne(
    @PrimaryKey
    var id: Long,

    @ColumnInfo(name = "is_edu_one", defaultValue = "NULL")
    val isEduOne: Boolean?,
) : Serializable
