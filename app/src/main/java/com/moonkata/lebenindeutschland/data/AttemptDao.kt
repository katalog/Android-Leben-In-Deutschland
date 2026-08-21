package com.moonkata.lebenindeutschland.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {
    @Insert
    suspend fun insert(attempt: Attempt): Long

    @Update
    suspend fun update(attempt: Attempt)

    @Query("SELECT * FROM attempts WHERE id = :id")
    suspend fun getById(id: Long): Attempt?

    @Query("SELECT * FROM attempts WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC")
    fun history(): Flow<List<Attempt>>

    @Query("SELECT * FROM attempts WHERE finishedAt IS NOT NULL AND mode = 'EXAM' ORDER BY startedAt DESC")
    fun examHistory(): Flow<List<Attempt>>
}
