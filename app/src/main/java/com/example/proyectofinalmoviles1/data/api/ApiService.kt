package com.example.proyectofinalmoviles1.data.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/logout")
    suspend fun logout(): Response<LogoutResponse>

    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @GET("api/groups")
    suspend fun getGroups(): Response<List<GroupResponse>>

    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<CreateGroupResponse>

    @POST("api/groups/join")
    suspend fun joinGroup(@Body request: JoinGroupRequest): Response<JoinGroupResponse>

    @GET("api/groups/{id}")
    suspend fun getGroupDetail(@Path("id") groupId: Int): Response<GroupDetailResponse>

    @GET("api/groups/{id}/leaderboard")
    suspend fun getLeaderboard(@Path("id") groupId: Int): Response<List<LeaderboardEntry>>

    @GET("api/matches")
    suspend fun getMatches(
        @Query("next") next: Boolean? = null,
        @Query("phase") phase: String? = null,
        @Query("status") status: String? = null,
        @Query("date") date: String? = null
    ): Response<List<MatchResponse>>

    @GET("api/matches/updates")
    suspend fun getMatchUpdates(@Query("since") since: String? = null): Response<MatchesUpdateResponse>

    @GET("api/matches/{id}")
    suspend fun getMatchDetail(@Path("id") matchId: Int): Response<MatchResponse>

    @POST("api/predictions")
    suspend fun createPrediction(@Body request: PredictionRequest): Response<PredictionResponse>

    @GET("api/predictions/me")
    suspend fun getMyPredictions(): Response<List<MyPredictionResponse>>

    @GET("api/stadiums")
    suspend fun getStadiums(): Response<List<StadiumResponse>>

    @GET("api/stadiums/{id}")
    suspend fun getStadiumDetail(@Path("id") stadiumId: Int): Response<StadiumResponse>

    @GET("api/stadiums/{id}/matches")
    suspend fun getStadiumMatches(@Path("id") stadiumId: Int): Response<List<MatchResponse>>

    @GET("up")
    suspend fun healthCheck(): Response<Unit>
}
