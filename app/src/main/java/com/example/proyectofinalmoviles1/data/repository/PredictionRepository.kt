package com.example.proyectofinalmoviles1.data.repository

import com.example.proyectofinalmoviles1.data.api.ApiService
import com.example.proyectofinalmoviles1.data.api.MyPredictionResponse
import com.example.proyectofinalmoviles1.data.api.PredictionRequest
import com.example.proyectofinalmoviles1.data.api.PredictionResponse
import com.example.proyectofinalmoviles1.data.local.dao.PredictionDao
import com.example.proyectofinalmoviles1.data.local.entity.PredictionEntity

class PredictionRepository(
    private val api: ApiService,
    private val predictionDao: PredictionDao
) {
    suspend fun createPrediction(matchId: Int, homeScore: Int, awayScore: Int): Result<PredictionResponse> {
        return try {
            val response = api.createPrediction(PredictionRequest(matchId, homeScore, awayScore))
            if (response.isSuccessful) {
                val body = response.body()!!
                predictionDao.insertPredictions(listOf(
                    PredictionEntity(
                        body.prediction.id, body.prediction.match_id,
                        body.prediction.home_score, body.prediction.away_score,
                        null, body.prediction.status
                    )
                ))
                Result.success(body)
            } else {
                val msg = try {
                    val gson = com.google.gson.Gson()
                    gson.fromJson(response.errorBody()?.string(), com.example.proyectofinalmoviles1.data.api.ErrorResponse::class.java)?.message
                } catch (_: Exception) { null }
                Result.failure(Exception(msg ?: "No se puede pronosticar este partido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyPredictions(): Result<List<MyPredictionResponse>> {
        return try {
            val response = api.getMyPredictions()
            if (response.isSuccessful) {
                val predictions = response.body()!!
                predictionDao.deleteAll()
                predictionDao.insertPredictions(predictions.map {
                    PredictionEntity(it.id, it.match_id, it.home_score, it.away_score, it.points_earned, it.status)
                })
                Result.success(predictions)
            } else {
                Result.failure(Exception("Error al obtener pronósticos"))
            }
        } catch (e: Exception) {
            val cached = predictionDao.getAllPredictions()
            if (cached.isNotEmpty()) {
                Result.success(cached.map {
                    MyPredictionResponse(it.id, it.matchId, it.homeScore, it.awayScore, it.pointsEarned, it.status,
                        com.example.proyectofinalmoviles1.data.api.MatchResponse(0, "", "", "", "", null, "", null, null, null))
                })
            } else {
                Result.failure(e)
            }
        }
    }
}
