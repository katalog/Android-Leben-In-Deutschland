package com.moonkata.lebenindeutschland.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int

    @Query("SELECT * FROM questions")
    suspend fun getAll(): List<Question>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: Int): Question?

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<Question>

    @Query("SELECT * FROM questions WHERE topicId = :topicId ORDER BY RANDOM() LIMIT :limit")
    suspend fun randomByTopic(topicId: String, limit: Int): List<Question>

    @Query("SELECT * FROM questions WHERE category = 'GENERAL' ORDER BY RANDOM() LIMIT :limit")
    suspend fun randomGeneral(limit: Int): List<Question>

    @Query("SELECT * FROM questions WHERE category = 'BUNDESLAND' AND bundesland = :code ORDER BY RANDOM() LIMIT :limit")
    suspend fun randomBundesland(code: String, limit: Int): List<Question>

    @Query("SELECT COUNT(*) FROM questions WHERE topicId = :topicId")
    suspend fun countByTopic(topicId: String): Int
}
