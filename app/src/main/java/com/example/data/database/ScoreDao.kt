package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 10")
    fun getTopScores(): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM high_scores WHERE difficulty = :difficulty ORDER BY score DESC LIMIT 10")
    fun getTopScoresByDifficulty(difficulty: String): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity)

    @Query("DELETE FROM high_scores")
    suspend fun clearAllScores()
}
