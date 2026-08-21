package com.moonkata.lebenindeutschland.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(
    entities = [Question::class, Attempt::class, AttemptAnswer::class, TranslationCacheEntry::class],
    version = 1,
)
abstract class LidDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun attemptAnswerDao(): AttemptAnswerDao
    abstract fun translationCacheDao(): TranslationCacheDao

    companion object {
        @Volatile private var instance: LidDatabase? = null
        private val seedMutex = Mutex()

        fun get(context: Context): LidDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, LidDatabase::class.java, "lid.db")
                    .build()
                    .also { instance = it }
            }

        /**
         * Suspends until the question catalogue is in the DB. Callers must await this before
         * running any question query — a Room onCreate Callback would populate asynchronously
         * with no way for the first screen's queries to wait for it, which raced and briefly
         * showed 0 questions.
         */
        suspend fun ensureSeeded(context: Context) {
            val db = get(context)
            if (db.questionDao().count() > 0) return
            seedMutex.withLock {
                if (db.questionDao().count() > 0) return
                db.questionDao().insertAll(QuestionAssetLoader.loadAll(context.applicationContext))
            }
        }
    }
}
