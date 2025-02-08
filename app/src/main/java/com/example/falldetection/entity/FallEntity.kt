package com.example.falldetection.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "falls")
data class FallEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val message: String
)
