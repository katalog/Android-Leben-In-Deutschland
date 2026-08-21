package com.moonkata.lebenindeutschland.data.translation

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.moonkata.lebenindeutschland.data.LidDatabase
import com.moonkata.lebenindeutschland.data.QuestionRepository
import com.moonkata.lebenindeutschland.data.TranslationCacheEntry
import com.moonkata.lebenindeutschland.data.TranslationContentType

/**
 * Downloads the DE-><language> ML Kit model, then translates every question/answer/explanation
 * once and writes it to TranslationCacheEntry — after this, FrageScreen reads translations
 * straight from Room with no further ML Kit calls (see plan.md "온디바이스 AI 번역").
 */
class PreTranslateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val languageCode = inputData.getString(KEY_LANGUAGE) ?: return Result.failure()
        val repository = QuestionRepository(LidDatabase.get(applicationContext))
        return try {
            TranslationEngine.downloadModel(languageCode)

            val fields = listOf(
                TranslationContentType.QUESTION to { q: com.moonkata.lebenindeutschland.data.Question -> q.textDe },
                TranslationContentType.ANSWER_A to { q -> q.answerA },
                TranslationContentType.ANSWER_B to { q -> q.answerB },
                TranslationContentType.ANSWER_C to { q -> q.answerC },
                TranslationContentType.ANSWER_D to { q -> q.answerD },
            )

            var batch = mutableListOf<TranslationCacheEntry>()
            for (question in repository.allQuestions()) {
                for ((type, textOf) in fields) {
                    if (repository.cachedTranslation(question.id, type, languageCode) != null) continue
                    val translated = TranslationEngine.translate(textOf(question), languageCode)
                    batch.add(TranslationCacheEntry(question.id, type, languageCode, translated))
                }
                val explanation = question.explanationDe
                if (explanation != null && repository.cachedTranslation(question.id, TranslationContentType.EXPLANATION, languageCode) == null) {
                    val translated = TranslationEngine.translate(explanation, languageCode)
                    batch.add(TranslationCacheEntry(question.id, TranslationContentType.EXPLANATION, languageCode, translated))
                }
                if (batch.size >= 50) {
                    repository.cacheTranslations(batch)
                    batch = mutableListOf()
                }
            }
            repository.cacheTranslations(batch)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val KEY_LANGUAGE = "language"

        fun enqueue(context: Context, languageCode: String) {
            val request = OneTimeWorkRequestBuilder<PreTranslateWorker>()
                .setInputData(workDataOf(KEY_LANGUAGE to languageCode))
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork("pretranslate_$languageCode", ExistingWorkPolicy.KEEP, request)
        }
    }
}
