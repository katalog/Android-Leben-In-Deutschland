package com.moonkata.lebenindeutschland.data.translation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.moonkata.lebenindeutschland.data.Languages
import com.moonkata.lebenindeutschland.data.LidDatabase
import com.moonkata.lebenindeutschland.data.Question
import com.moonkata.lebenindeutschland.data.QuestionRepository
import com.moonkata.lebenindeutschland.data.TranslationCacheEntry
import com.moonkata.lebenindeutschland.data.TranslationContentType

/**
 * Downloads the DE-><language> ML Kit model, then translates every question/answer/explanation
 * once and writes it to TranslationCacheEntry — after this, FrageScreen reads translations
 * straight from Room with no further ML Kit calls (see plan.md "온디바이스 AI 번역").
 *
 * Runs as a foreground service with a progress notification, and reports progress via
 * [setProgress] so MehrScreen's Sprachmodelle list can update live instead of only after a
 * manual refresh.
 */
class PreTranslateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val languageCode = inputData.getString(KEY_LANGUAGE)
    private val languageNative = Languages.all.find { it.code == languageCode }?.native ?: languageCode.orEmpty()

    override suspend fun doWork(): Result {
        val languageCode = languageCode ?: return Result.failure()
        val repository = QuestionRepository(LidDatabase.get(applicationContext))
        ensureChannel()
        setForeground(foregroundInfo(0, TOTAL_FIELDS))

        return try {
            TranslationEngine.downloadModel(languageCode)

            val fields = listOf(
                TranslationContentType.QUESTION to { q: Question -> q.textDe },
                TranslationContentType.ANSWER_A to { q -> q.answerA },
                TranslationContentType.ANSWER_B to { q -> q.answerB },
                TranslationContentType.ANSWER_C to { q -> q.answerC },
                TranslationContentType.ANSWER_D to { q -> q.answerD },
            )

            var batch = mutableListOf<TranslationCacheEntry>()
            var done = repository.translationCoverage(languageCode)
            for (question in repository.allQuestions()) {
                for ((type, textOf) in fields) {
                    if (repository.cachedTranslation(question.id, type, languageCode) != null) continue
                    val translated = TranslationEngine.translate(textOf(question), languageCode)
                    batch.add(TranslationCacheEntry(question.id, type, languageCode, translated))
                    done++
                }
                val explanation = question.explanationDe
                if (explanation != null && repository.cachedTranslation(question.id, TranslationContentType.EXPLANATION, languageCode) == null) {
                    val translated = TranslationEngine.translate(explanation, languageCode)
                    batch.add(TranslationCacheEntry(question.id, TranslationContentType.EXPLANATION, languageCode, translated))
                    done++
                }
                if (batch.size >= 50) {
                    repository.cacheTranslations(batch)
                    batch = mutableListOf()
                    setProgress(workDataOf(KEY_DONE to done, KEY_TOTAL to TOTAL_FIELDS))
                    setForeground(foregroundInfo(done, TOTAL_FIELDS))
                }
            }
            repository.cacheTranslations(batch)
            setProgress(workDataOf(KEY_DONE to TOTAL_FIELDS, KEY_TOTAL to TOTAL_FIELDS))
            notificationManager.cancel(languageCode.hashCode())
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "Sprachpakete", NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private fun notification(done: Int, total: Int): Notification =
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Sprachpaket wird geladen: $languageNative")
            .setContentText("$done / $total")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(total, done, false)
            .setOngoing(true)
            .build()

    private fun foregroundInfo(done: Int, total: Int): ForegroundInfo {
        val id = (languageCode ?: "").hashCode()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(id, notification(done, total), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(id, notification(done, total))
        }
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val CHANNEL_ID = "translation_downloads"
        const val TOTAL_FIELDS = 460 * 5

        const val KEY_DONE = "done"
        const val KEY_TOTAL = "total"

        fun workName(languageCode: String) = "pretranslate_$languageCode"

        fun enqueue(context: Context, languageCode: String) {
            val request = OneTimeWorkRequestBuilder<PreTranslateWorker>()
                .setInputData(workDataOf(KEY_LANGUAGE to languageCode))
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(workName(languageCode), ExistingWorkPolicy.KEEP, request)
        }
    }
}
