package com.example.proyectofinalmoviles1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_info")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val participantsCount: Int,
    val userScore: Int,
    val inviteCode: String
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val matchDate: String,
    val phase: String,
    val groupName: String?,
    val status: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val stadium: String?
)

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey val id: Int,
    val matchId: Int,
    val homeScore: Int,
    val awayScore: Int,
    val pointsEarned: Int?,
    val status: String
)

@Entity(tableName = "stadiums")
data class StadiumEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int
)

@Entity(tableName = "participants")
data class ParticipantEntity(
    @PrimaryKey val id: Int,
    val groupId: Int,
    val name: String,
    val score: Int
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
)
