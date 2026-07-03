package com.example.data.repository

import com.example.data.database.ScoreDao
import com.example.data.database.ScoreEntity
import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {
    val topScores: Flow<List<ScoreEntity>> = scoreDao.getTopScores()

    fun getTopScoresByDifficulty(difficulty: String): Flow<List<ScoreEntity>> {
        return scoreDao.getTopScoresByDifficulty(difficulty)
    }

    suspend fun insertScore(score: ScoreEntity) {
        scoreDao.insertScore(score)
    }

    suspend fun clearAllScores() {
        scoreDao.clearAllScores()
    }
}
