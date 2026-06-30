package com.example.proyectofinalmoviles1.data.repository

import com.example.proyectofinalmoviles1.data.api.*
import com.example.proyectofinalmoviles1.data.local.dao.UserDao
import com.example.proyectofinalmoviles1.data.local.entity.UserEntity
import com.example.proyectofinalmoviles1.data.preferences.AuthDataStore

class AuthRepository(
    private val api: ApiService,
    private val dataStore: AuthDataStore,
    private val userDao: UserDao
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                dataStore.saveAuthData(body.token, body.name, body.email)
                userDao.insertUser(UserEntity(name = body.name, email = body.email))
                Result.success(body)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String, passwordConfirmation: String): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(name, email, password, passwordConfirmation))
            if (response.isSuccessful) {
                val body = response.body()!!
                dataStore.saveAuthData(body.token, body.name, body.email)
                userDao.insertUser(UserEntity(name = body.name, email = body.email))
                Result.success(body)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(token: String): Result<Unit> {
        return try {
            val response = api.logout()
            dataStore.clearSession()
            userDao.deleteUser()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Logout failed"))
        } catch (e: Exception) {
            dataStore.clearSession()
            userDao.deleteUser()
            Result.success(Unit)
        }
    }

    suspend fun getStoredToken(): String? = dataStore.getToken()

    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) return "Error del servidor (${response.code()})"
            if (!errorBody.trimStart().startsWith("{")) return "Error del servidor (${response.code()})"
            val gson = com.google.gson.GsonBuilder().setLenient().create()
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            errorResponse?.message ?: "Error del servidor (${response.code()})"
        } catch (_: Exception) {
            "Error de conexión (${response.code()})"
        }
    }
}
