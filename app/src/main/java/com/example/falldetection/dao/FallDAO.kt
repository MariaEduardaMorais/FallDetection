package com.example.falldetection.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.falldetection.entity.FallEntity

@Dao
interface FallDao {
    @Insert
    fun insertFall(fall: FallEntity)

    @Query("SELECT * FROM falls ORDER BY timestamp DESC")
    fun getAllFalls(): List<FallEntity>
}
