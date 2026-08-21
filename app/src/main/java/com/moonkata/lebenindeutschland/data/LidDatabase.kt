package com.moonkata.lebenindeutschland.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        fun get(context: Context): LidDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): LidDatabase {
            val appContext = context.applicationContext
            return Room.databaseBuilder(appContext, LidDatabase::class.java, "lid.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate the static question catalogue on first launch only.
                        CoroutineScope(Dispatchers.IO).launch {
                            get(appContext).questionDao().insertAll(QuestionAssetLoader.loadAll(appContext))
                        }
                    }
                })
                .build()
        }
    }
}
