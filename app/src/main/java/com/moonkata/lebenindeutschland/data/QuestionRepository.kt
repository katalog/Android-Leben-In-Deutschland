package com.moonkata.lebenindeutschland.data

class QuestionRepository(private val db: LidDatabase) {
    fun attemptHistory() = db.attemptDao().history()


    suspend fun randomGeneral(count: Int): List<Question> = db.questionDao().randomGeneral(count)

    suspend fun randomBundesland(code: String, count: Int): List<Question> =
        db.questionDao().randomBundesland(code, count)

    /** Practice sessions are capped at 10 questions per topic, same as a Bundesland round, so a session stays a short focused round instead of dumping the whole topic (up to 70 questions) at once. */
    suspend fun randomByTopic(topicId: String, count: Int = 10): List<Question> = db.questionDao().randomByTopic(topicId, count)

    suspend fun byIds(ids: List<Int>): List<Question> = db.questionDao().getByIds(ids)

    /** 30 general + 3 Bundesland-specific, per the real exam's composition. Falls back to 33 general if no Bundesland is set. */
    suspend fun examQuestions(bundesland: String?): List<Question> {
        val general = db.questionDao().randomGeneral(if (bundesland != null) 30 else 33)
        val regional = bundesland?.let { db.questionDao().randomBundesland(it, 3) }.orEmpty()
        return (general + regional).shuffled()
    }

    suspend fun topicTotal(topicId: String): Int = db.questionDao().countByTopic(topicId)

    suspend fun topicCorrect(topicId: String): Int = db.attemptAnswerDao().correctDistinctCountByTopic(topicId)

    suspend fun overallTotal(): Int = db.questionDao().count()

    suspend fun overallCorrect(): Int = db.attemptAnswerDao().correctDistinctQuestionCount()

    /**
     * Questions whose most recent answer (across every practice/exam/review attempt ever) was
     * wrong — the persistent wrong-answer queue, as opposed to Ergebnis's session-only list.
     * Reduced in Kotlin, same reasoning as AttemptAnswerDao.allOrderedByTime.
     */
    suspend fun globalWrongQuestions(): List<Question> {
        val latestByQuestion = LinkedHashMap<Int, Boolean>()
        db.attemptAnswerDao().allOrderedByTime().forEach { answer ->
            latestByQuestion[answer.questionId] = answer.isCorrect
        }
        val wrongIds = latestByQuestion.filterValues { !it }.keys.toList()
        return db.questionDao().getByIds(wrongIds)
    }

    suspend fun startAttempt(mode: AttemptMode, bundesland: String?, totalQuestions: Int): Attempt {
        val attempt = Attempt(
            mode = mode,
            bundesland = bundesland,
            startedAt = System.currentTimeMillis(),
            finishedAt = null,
            totalQuestions = totalQuestions,
            correctCount = 0,
            passed = null,
            durationSeconds = null,
        )
        val id = db.attemptDao().insert(attempt)
        return attempt.copy(id = id)
    }

    suspend fun recordAnswer(attemptId: Long, questionId: Int, pickedAnswerIndex: Int, isCorrect: Boolean) {
        db.attemptAnswerDao().insertAll(
            listOf(
                AttemptAnswer(
                    attemptId = attemptId,
                    questionId = questionId,
                    pickedAnswerIndex = pickedAnswerIndex,
                    isCorrect = isCorrect,
                    answeredAt = System.currentTimeMillis(),
                )
            )
        )
    }

    suspend fun finishAttempt(attempt: Attempt, correctCount: Int, passed: Boolean?) {
        val finishedAt = System.currentTimeMillis()
        db.attemptDao().update(
            attempt.copy(
                finishedAt = finishedAt,
                correctCount = correctCount,
                passed = passed,
                durationSeconds = ((finishedAt - attempt.startedAt) / 1000).toInt(),
            )
        )
    }
}
