package com.example.proyectofinalmoviles1.data.repository

import com.example.proyectofinalmoviles1.data.api.ApiService
import com.example.proyectofinalmoviles1.data.api.MatchResponse
import com.example.proyectofinalmoviles1.data.api.StadiumResponse
import com.example.proyectofinalmoviles1.data.api.apiExceptionMessage
import com.example.proyectofinalmoviles1.data.api.parseApiError
import com.example.proyectofinalmoviles1.data.local.dao.MatchDao
import com.example.proyectofinalmoviles1.data.local.dao.StadiumDao
import com.example.proyectofinalmoviles1.data.local.entity.StadiumEntity

class StadiumRepository(
    private val api: ApiService,
    private val stadiumDao: StadiumDao,
    private val matchDao: MatchDao
) {
    suspend fun getStadiums(): Result<List<StadiumResponse>> {
        return try {
            val response = api.getStadiums()
            if (response.isSuccessful) {
                val stadiums = response.body()!!
                stadiumDao.deleteAll()
                stadiumDao.insertStadiums(stadiums.map {
                    StadiumEntity(it.id, it.name, it.city, it.country, it.latitude, it.longitude, it.capacity)
                })
                Result.success(stadiums)
            } else {
                Result.failure(Exception(parseApiError(response, "Error al obtener estadios")))
            }
        } catch (e: Exception) {
            val cached = stadiumDao.getAllStadiums()
            if (cached.isNotEmpty()) {
                Result.success(cached.map {
                    StadiumResponse(it.id, it.name, it.city, it.country, it.latitude, it.longitude, it.capacity)
                })
            } else {
                Result.failure(Exception(apiExceptionMessage(e, "Error al obtener estadios")))
            }
        }
    }

    suspend fun getStadiumDetail(stadiumId: Int): Result<StadiumResponse> {
        return try {
            val response = api.getStadiumDetail(stadiumId)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception(parseApiError(response, "Estadio no encontrado")))
        } catch (e: Exception) {
            val cached = stadiumDao.getStadium(stadiumId)
            if (cached != null) {
                Result.success(StadiumResponse(cached.id, cached.name, cached.city, cached.country, cached.latitude, cached.longitude, cached.capacity))
            } else {
                Result.failure(Exception(apiExceptionMessage(e, "Estadio no encontrado")))
            }
        }
    }

    suspend fun getStadiumMatches(stadiumId: Int): Result<List<MatchResponse>> {
        return try {
            val response = api.getStadiumMatches(stadiumId)
            if (response.isSuccessful) {
                val matches = response.body()!!
                matchDao.insertMatches(matches.map { it.toEntity() })
                Result.success(matches)
            } else Result.failure(Exception(parseApiError(response, "Error al obtener partidos del estadio")))
        } catch (e: Exception) {
            Result.failure(Exception(apiExceptionMessage(e, "Error al obtener partidos del estadio")))
        }
    }

    private fun MatchResponse.toEntity() = com.example.proyectofinalmoviles1.data.local.entity.MatchEntity(
        id, home_team, away_team, match_date, phase,
        group_name, status, home_score, away_score, stadium
    )
}
