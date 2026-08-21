package com.moonkata.lebenindeutschland.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AttemptMode { PRACTICE, EXAM, REVIEW }

/** One practice session, mock exam, or wrong-answer review run. */
@Entity(tableName = "attempts")
data class Attempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: AttemptMode,
    /** Bundesland used for EXAM's 3 state-specific questions, or the practiced topic's Bundesland. */
    val bundesland: String?,
    val startedAt: Long,
    val finishedAt: Long?,
    val totalQuestions: Int,
    val correctCount: Int,
    /** Null until finished; for EXAM, correctCount >= 17 of 33. */
    val passed: Boolean?,
    val durationSeconds: Int?,
)
