package com.moonkata.lebenindeutschland.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class QuestionCategory { GENERAL, BUNDESLAND }

/** One of the 460 fixed catalogue questions (300 general + 16 Bundesländer × 10). See assets/questions/SOURCE.md. */
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val id: Int,
    val category: QuestionCategory,
    /** Two-letter code (e.g. "BY") when [category] is BUNDESLAND, else null. */
    val bundesland: String?,
    val topicId: String,
    val textDe: String,
    val answerA: String,
    val answerB: String,
    val answerC: String,
    val answerD: String,
    /** 0-3, index into answerA..D. */
    val correctAnswerIndex: Int,
    val explanationDe: String?,
    /** Filename under assets/images/, e.g. "aufgabe_21.png". */
    val imageAsset: String?,
    val imageCaption: String?,
)
