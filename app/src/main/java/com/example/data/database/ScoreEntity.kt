package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pilotName: String,
    val score: Int,
    val difficulty: String,
    val timestamp: Long = System.currentTimeMillis()
)
