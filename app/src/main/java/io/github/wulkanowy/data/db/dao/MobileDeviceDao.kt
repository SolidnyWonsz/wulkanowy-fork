package io.github.wulkanowy.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import io.github.wulkanowy.data.db.entities.MobileDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface MobileDeviceDao : BaseDao<MobileDevice> {

    @Query("SELECT * FROM MobileDevices WHERE user_login_id = :studentId ORDER BY date DESC")
    fun loadAll(studentId: Int): Flow<List<MobileDevice>>
}
