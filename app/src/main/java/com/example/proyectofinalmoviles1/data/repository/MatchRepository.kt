package com.example.proyectofinalmoviles1.data.repository

import com.example.proyectofinalmoviles1.data.api.ApiService
import com.example.proyectofinalmoviles1.data.api.MatchResponse
import com.example.proyectofinalmoviles1.data.local.dao.MatchDao
import com.example.proyectofinalmoviles1.data.local.entity.MatchEntity

class MatchRepository(
    private val api: ApiService,
    private val matchDao: MatchDao
) {
    suspend fun getMatches(
        next: Boolean? = null,
        phase: String? = null,
        status: String? = null,
        date: String? = null
    ): Result<List<MatchResponse>> {
        return try {
            val response = api.getMatches(next, phase, status, date)
            if (response.isSuccessful) {
                val matches = response.body()!!
                if (next == null && phase == null && status == null && date == null) {
                    matchDao.deleteAll()
                    matchDao.insertMatches(matches.map { it.toEntity() })
                }
                Result.success(matches)
            } else {
                Result.failure(Exception("Error al obtener partidos"))
            }
        } catch (e: Exception) {
            if (next == true) {
                val cached = matchDao.getNextMatches()
                if (cached.isNotEmpty()) {
                    return Result.success(cached.map { it.toResponse() })
                }
            }
            val cached = matchDao.getAllMatches()
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toResponse() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getMatchDetail(matchId: Int): Result<MatchResponse> {
        return try {
            val response = api.getMatchDetail(matchId)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Partido no encontrado"))
        } catch (e: Exception) {
            val cached = matchDao.getMatch(matchId)
            if (cached != null) Result.success(cached.toResponse())
            else Result.failure(e)
        }
    }

    suspend fun getUpdates(since: String?): Result<com.example.proyectofinalmoviles1.data.api.MatchesUpdateResponse> {
        return try {
            val response = api.getMatchUpdates(since)
            if (response.isSuccessful) {
                val body = response.body()!!
                matchDao.insertMatches(body.matches.map { it.toEntity() })
                Result.success(body)
            } else {
                Result.failure(Exception("Error al obtener actualizaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun MatchResponse.toEntity() = MatchEntity(
        id, home_team, away_team, match_date, phase,
        group_name, status, home_score, away_score, stadium
    )

    private fun MatchEntity.toResponse() = MatchResponse(
        id, homeTeam, awayTeam, matchDate, phase,
        groupName, status, homeScore, awayScore, stadium
    )
}
