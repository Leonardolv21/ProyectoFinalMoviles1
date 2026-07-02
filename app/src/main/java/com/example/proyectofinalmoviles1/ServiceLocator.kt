package com.example.proyectofinalmoviles1

import android.content.Context
import com.example.proyectofinalmoviles1.data.TokenProvider
import com.example.proyectofinalmoviles1.data.api.ApiClient
import com.example.proyectofinalmoviles1.data.local.AppDatabase
import com.example.proyectofinalmoviles1.data.preferences.AuthDataStore
import com.example.proyectofinalmoviles1.data.repository.*
import kotlinx.coroutines.runBlocking

object ServiceLocator {
    private lateinit var database: AppDatabase
    private lateinit var dataStore: AuthDataStore

    val authRepository by lazy { AuthRepository(ApiClient.apiService, dataStore, database.userDao()) }
    val profileRepository by lazy { ProfileRepository(ApiClient.apiService, database.userDao()) }
    val groupRepository by lazy { GroupRepository(ApiClient.apiService, database.groupDao(), database.participantDao()) }
    val matchRepository by lazy { MatchRepository(ApiClient.apiService, database.matchDao(), database.syncMetadataDao()) }
    val predictionRepository by lazy { PredictionRepository(ApiClient.apiService, database.predictionDao()) }
    val stadiumRepository by lazy { StadiumRepository(ApiClient.apiService, database.stadiumDao(), database.matchDao()) }

    fun init(context: Context) {
        database = AppDatabase.getInstance(context)
        dataStore = AuthDataStore(context)
        val savedToken = runBlocking { dataStore.getToken() }
        if (!savedToken.isNullOrBlank()) {
            TokenProvider.token = savedToken
        }
    }
}
