package com.example.proyectofinalmoviles1.data.repository

import com.example.proyectofinalmoviles1.data.api.ApiService
import com.example.proyectofinalmoviles1.data.api.ProfileResponse
import com.example.proyectofinalmoviles1.data.local.dao.UserDao
import com.example.proyectofinalmoviles1.data.local.entity.UserEntity

class ProfileRepository(
    private val api: ApiService,
    private val userDao: UserDao
) {
    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                val body = response.body()!!
                userDao.insertUser(UserEntity(name = body.name, email = body.email))
                Result.success(body)
            } else {
                Result.failure(Exception("Error al obtener perfil"))
            }
        } catch (e: Exception) {
            val cached = userDao.getUser()
            if (cached != null) {
                Result.success(ProfileResponse(cached.name, cached.email, 0, 0, 0))
            } else {
                Result.failure(e)
            }
        }
    }
}
