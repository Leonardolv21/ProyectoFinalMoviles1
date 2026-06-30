package com.example.proyectofinalmoviles1.data.repository

import com.example.proyectofinalmoviles1.data.api.ApiService
import com.example.proyectofinalmoviles1.data.api.MatchResponse
import com.example.proyectofinalmoviles1.data.api.StadiumResponse
import com.example.proyectofinalmoviles1.data.local.dao.MatchDao
import com.example.proyectofinalmoviles1.data.local.dao.StadiumDao
import com.example.proyectofinalmoviles1.data.local.entity.MatchEntity
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
                Result.failure(Exception("Error al obtener estadios"))
            }
        } catch (e: Exception) {
            val cached = stadiumDao.getAllStadiums()
            if (cached.isNotEmpty()) {
                Result.success(cached.map {
                    StadiumResponse(it.id, it.name, it.city, it.country, it.latitude, it.longitude, it.capacity)
                })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getStadiumDetail(stadiumId: Int): Result<StadiumResponse> {
        return try {
            val response = api.getStadiumDetail(stadiumId)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Estadio no encontrado"))
        } catch (e: Exception) {
            val cached = stadiumDao.getStadium(stadiumId)
            if (cached != null) {
                Result.success(StadiumResponse(cached.id, cached.name, cached.city, cached.country, cached.latitude, cached.longitude, cached.capacity))
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getStadiumMatches(stadiumId: Int): Result<List<MatchResponse>> {
        return try {
            val response = api.getStadiumMatches(stadiumId)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener partidos del estadio"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
