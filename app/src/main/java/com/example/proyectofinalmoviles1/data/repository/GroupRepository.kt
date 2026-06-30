package com.example.proyectofinalmoviles1.data.repository

import com.example.proyectofinalmoviles1.data.api.*
import com.example.proyectofinalmoviles1.data.local.dao.GroupDao
import com.example.proyectofinalmoviles1.data.local.dao.ParticipantDao
import com.example.proyectofinalmoviles1.data.local.entity.GroupEntity
import com.example.proyectofinalmoviles1.data.local.entity.ParticipantEntity

class GroupRepository(
    private val api: ApiService,
    private val groupDao: GroupDao,
    private val participantDao: ParticipantDao
) {
    suspend fun getGroups(): Result<List<GroupResponse>> {
        return try {
            val response = api.getGroups()
            if (response.isSuccessful) {
                val groups = response.body()!!
                groupDao.deleteAll()
                groupDao.insertGroups(groups.map {
                    GroupEntity(it.id, it.name, it.participants_count, it.user_score, it.invite_code)
                })
                Result.success(groups)
            } else {
                Result.failure(Exception("Error al obtener grupos"))
            }
        } catch (e: Exception) {
            val cached = groupDao.getAllGroups()
            if (cached.isNotEmpty()) {
                Result.success(cached.map {
                    GroupResponse(it.id, it.name, it.participantsCount, it.userScore, it.inviteCode)
                })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun createGroup(name: String): Result<CreateGroupResponse> {
        return try {
            val response = api.createGroup(CreateGroupRequest(name))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al crear grupo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroup(inviteCode: String): Result<JoinGroupResponse> {
        return try {
            val response = api.joinGroup(JoinGroupRequest(inviteCode))
            if (response.isSuccessful) Result.success(response.body()!!)
            else {
                val msg = try {
                    val gson = com.google.gson.Gson()
                    gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)?.message
                } catch (_: Exception) { null }
                Result.failure(Exception(msg ?: "Error al unirse al grupo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupDetail(groupId: Int): Result<GroupDetailResponse> {
        return try {
            val response = api.getGroupDetail(groupId)
            if (response.isSuccessful) {
                val body = response.body()!!
                participantDao.deleteByGroup(groupId)
                participantDao.insertParticipants(body.participants.map {
                    ParticipantEntity(it.id, groupId, it.name, it.score)
                })
                Result.success(body)
            } else {
                Result.failure(Exception("No autorizado o grupo no encontrado"))
            }
        } catch (e: Exception) {
            val cached = participantDao.getParticipants(groupId)
            if (cached.isNotEmpty()) {
                Result.success(
                    GroupDetailResponse(
                        groupId, "", "", cached.map {
                            ParticipantResponse(it.id, it.name, it.score)
                        }, emptyList()
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getLeaderboard(groupId: Int): Result<List<LeaderboardEntry>> {
        return try {
            val response = api.getLeaderboard(groupId)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener clasificación"))
        } catch (e: Exception) {
            val cached = participantDao.getParticipants(groupId)
            if (cached.isNotEmpty()) {
                Result.success(cached.mapIndexed { i, p ->
                    LeaderboardEntry(i + 1, p.id, p.name, p.score)
                })
            } else {
                Result.failure(e)
            }
        }
    }
}
