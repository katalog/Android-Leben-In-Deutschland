package com.moonkata.lebenindeutschland.data.translation

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.moonkata.lebenindeutschland.data.Languages
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Thin coroutine wrapper around ML Kit's on-device Translation API (free, confirmed in
 * plan.md — no billing, fully offline once a language's model is downloaded).
 */
object TranslationEngine {
    private val modelManager by lazy { RemoteModelManager.getInstance() }
    private val translators = mutableMapOf<String, Translator>()

    private fun translatorFor(appLanguageCode: String): Translator =
        translators.getOrPut(appLanguageCode) {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(LanguageCode.GERMAN)
                    .setTargetLanguage(LanguageCode.mlKit(appLanguageCode))
                    .build()
            )
        }

    suspend fun isModelDownloaded(appLanguageCode: String): Boolean {
        val model = remoteModel(appLanguageCode)
        return modelManager.isModelDownloaded(model).await()
    }

    /** Downloads the DE->[appLanguageCode] model. Requires internet once; translation itself never does. */
    suspend fun downloadModel(appLanguageCode: String, wifiOnly: Boolean = true) {
        val conditions = DownloadConditions.Builder().apply { if (wifiOnly) requireWifi() }.build()
        translatorFor(appLanguageCode).downloadModelIfNeeded(conditions).await()
    }

    suspend fun translate(text: String, appLanguageCode: String): String =
        translatorFor(appLanguageCode).translate(text).await()

    suspend fun downloadedLanguageCodes(): Set<String> {
        val models = modelManager.getDownloadedModels(TranslateRemoteModel::class.java).await()
        val mlKitToApp = Languages.all.associate { LanguageCode.mlKit(it.code) to it.code }
        return models.mapNotNull { mlKitToApp[it.language] }.toSet()
    }

    suspend fun deleteModel(appLanguageCode: String) {
        modelManager.deleteDownloadedModel(remoteModel(appLanguageCode)).await()
    }

    private fun remoteModel(appLanguageCode: String): TranslateRemoteModel =
        TranslateRemoteModel.Builder(LanguageCode.mlKit(appLanguageCode)).build()

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
    }
}
