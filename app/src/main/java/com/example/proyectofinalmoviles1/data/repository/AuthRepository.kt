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
                Result.failure(Exception(parseApiError(response, "Error al iniciar sesion")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(apiExceptionMessage(e, "Error al iniciar sesion")))
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
                Result.failure(Exception(parseApiError(response, "Error al registrar usuario")))
            }
        } catch (e: Exception) {
            Result.failure(Exception(apiExceptionMessage(e, "Error al registrar usuario")))
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

}
