package com.moonkata.lebenindeutschland.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AttemptAnswerDao {
    @Insert
    suspend fun insertAll(answers: List<AttemptAnswer>)

    @Query("SELECT * FROM attempt_answers WHERE attemptId = :attemptId ORDER BY answeredAt")
    suspend fun forAttempt(attemptId: Long): List<AttemptAnswer>

    /**
     * All answers ordered oldest-first. The wrong-answer queue (questions whose *latest* answer
     * was incorrect) is reduced from this in Kotlin rather than SQL — the catalogue and attempt
     * history are both small, and this avoids relying on window-function support that varies by
     * the device's bundled SQLite version (minSdk 26).
     */
    @Query("SELECT * FROM attempt_answers ORDER BY answeredAt")
    suspend fun allOrderedByTime(): List<AttemptAnswer>
}
