package com.moonkata.lebenindeutschland.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TranslationCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TranslationCacheEntry>)

    @Query(
        "SELECT * FROM translation_cache WHERE questionId = :questionId AND contentType = :contentType AND languageCode = :languageCode"
    )
    suspend fun get(questionId: Int, contentType: TranslationContentType, languageCode: String): TranslationCacheEntry?

    @Query("SELECT COUNT(*) FROM translation_cache WHERE languageCode = :languageCode")
    suspend fun countForLanguage(languageCode: String): Int

    @Query("DELETE FROM translation_cache WHERE languageCode = :languageCode")
    suspend fun deleteForLanguage(languageCode: String)
}
