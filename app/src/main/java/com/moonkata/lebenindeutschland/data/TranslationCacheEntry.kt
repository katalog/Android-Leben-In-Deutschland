package com.moonkata.lebenindeutschland.data

import androidx.room.Entity

enum class TranslationContentType { QUESTION, ANSWER_A, ANSWER_B, ANSWER_C, ANSWER_D, EXPLANATION }

/**
 * On-device ML Kit translation output, cached forever per (question, field, language) so the
 * fixed catalogue is only ever translated once per device. See plan.md "온디바이스 AI 번역".
 */
@Entity(tableName = "translation_cache", primaryKeys = ["questionId", "contentType", "languageCode"])
data class TranslationCacheEntry(
    val questionId: Int,
    val contentType: TranslationContentType,
    /** BCP-47 target language code, e.g. "ko", "ar", "tr". */
    val languageCode: String,
    val translatedText: String,
)
