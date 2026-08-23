package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ContainerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Query("SELECT * FROM containers ORDER BY gateInDate DESC")
    fun getAllContainers(): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE status = 'IN_YARD' ORDER BY gateInDate DESC")
    fun getContainersInYard(): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE status = 'GATED_OUT' ORDER BY gateOutDate DESC")
    fun getGatedOutContainers(): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE id = :id LIMIT 1")
    suspend fun getContainerById(id: Long): ContainerEntity?

    @Query("SELECT * FROM containers WHERE id = :id LIMIT 1")
    fun getContainerFlowById(id: Long): Flow<ContainerEntity?>

    @Query("SELECT * FROM containers WHERE containerNo LIKE '%' || :query || '%' OR bookingNo LIKE '%' || :query || '%' OR nocNo LIKE '%' || :query || '%' OR transporter LIKE '%' || :query || '%' OR shipper LIKE '%' || :query || '%' OR consignee LIKE '%' || :query || '%' OR driverCnic LIKE '%' || :query || '%'")
    fun searchContainers(query: String): Flow<List<ContainerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContainer(container: ContainerEntity): Long

    @Update
    suspend fun updateContainer(container: ContainerEntity)

    @Delete
    suspend fun deleteContainer(container: ContainerEntity)

    @Query("DELETE FROM containers WHERE id = :id")
    suspend fun deleteContainerById(id: Long)

    @Query("DELETE FROM containers")
    suspend fun deleteAllContainers()
}
