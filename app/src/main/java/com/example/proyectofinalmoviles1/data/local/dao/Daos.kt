package com.example.proyectofinalmoviles1.data.local.dao

import androidx.room.*
import com.example.proyectofinalmoviles1.data.local.entity.*

@Dao
interface UserDao {
    @Query("SELECT * FROM user_info WHERE id = 1")
    suspend fun getUser(): UserEntity?

    @Upsert
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM user_info")
    suspend fun deleteUser()
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM `groups`")
    suspend fun getAllGroups(): List<GroupEntity>

    @Upsert
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Query("DELETE FROM `groups`")
    suspend fun deleteAll()

    @Query("SELECT * FROM `groups` WHERE id = :groupId")
    suspend fun getGroup(groupId: Int): GroupEntity?
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches")
    suspend fun getAllMatches(): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE status = :status")
    suspend fun getMatchesByStatus(status: String): List<MatchEntity>

    @Query("SELECT * FROM matches ORDER BY matchDate ASC LIMIT :limit")
    suspend fun getNextMatches(limit: Int = 10): List<MatchEntity>

    @Upsert
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("DELETE FROM matches")
    suspend fun deleteAll()

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatch(id: Int): MatchEntity?
}

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions")
    suspend fun getAllPredictions(): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE matchId = :matchId LIMIT 1")
    suspend fun getPredictionByMatch(matchId: Int): PredictionEntity?

    @Upsert
    suspend fun insertPredictions(predictions: List<PredictionEntity>)

    @Query("DELETE FROM predictions")
    suspend fun deleteAll()
}

@Dao
interface StadiumDao {
    @Query("SELECT * FROM stadiums")
    suspend fun getAllStadiums(): List<StadiumEntity>

    @Upsert
    suspend fun insertStadiums(stadiums: List<StadiumEntity>)

    @Query("DELETE FROM stadiums")
    suspend fun deleteAll()

    @Query("SELECT * FROM stadiums WHERE id = :id")
    suspend fun getStadium(id: Int): StadiumEntity?
}

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants WHERE groupId = :groupId ORDER BY score DESC")
    suspend fun getParticipants(groupId: Int): List<ParticipantEntity>

    @Upsert
    suspend fun insertParticipants(participants: List<ParticipantEntity>)

    @Query("DELETE FROM participants WHERE groupId = :groupId")
    suspend fun deleteByGroup(groupId: Int)
}

@Dao
interface SyncMetadataDao {
    @Query("SELECT value FROM sync_metadata WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Upsert
    suspend fun upsert(metadata: SyncMetadataEntity)
}
