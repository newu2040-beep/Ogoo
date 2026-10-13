package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomLutDao {

    @Query("SELECT * FROM custom_luts ORDER BY createdAt DESC")
    fun getAllCustomLuts(): Flow<List<CustomLutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLut(lut: CustomLutEntity)

    @Update
    suspend fun updateCustomLut(lut: CustomLutEntity)

    @Delete
    suspend fun deleteCustomLut(lut: CustomLutEntity)

    @Query("DELETE FROM custom_luts WHERE id = :id")
    suspend fun deleteById(id: String)
}
