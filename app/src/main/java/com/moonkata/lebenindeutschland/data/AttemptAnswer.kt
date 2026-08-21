package com.moonkata.lebenindeutschland.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One answered question within an [Attempt]. The latest row per questionId drives the wrong-answer queue. */
@Entity(
    tableName = "attempt_answers",
    foreignKeys = [
        ForeignKey(entity = Attempt::class, parentColumns = ["id"], childColumns = ["attemptId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Question::class, parentColumns = ["id"], childColumns = ["questionId"]),
    ],
    indices = [Index("attemptId"), Index("questionId"), Index("answeredAt")],
)
data class AttemptAnswer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,
    val questionId: Int,
    val pickedAnswerIndex: Int,
    val isCorrect: Boolean,
    val answeredAt: Long,
)
