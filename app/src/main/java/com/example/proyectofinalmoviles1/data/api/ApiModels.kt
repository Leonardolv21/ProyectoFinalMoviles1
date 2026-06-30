package com.example.proyectofinalmoviles1.data.api

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class AuthResponse(val token: String, val name: String, val email: String)

data class ProfileResponse(
    val name: String,
    val email: String,
    val total_score: Int,
    val groups_count: Int,
    val predictions_count: Int
)

data class GroupResponse(
    val id: Int,
    val name: String,
    val participants_count: Int,
    val user_score: Int,
    val invite_code: String
)

data class CreateGroupRequest(val name: String)

data class CreateGroupResponse(
    val id: Int,
    val name: String,
    val invite_code: String,
    val created_at: String
)

data class JoinGroupRequest(val invite_code: String)

data class JoinGroupResponse(val message: String, val group: GroupBasicInfo)

data class GroupBasicInfo(val id: Int, val name: String, val participants_count: Int)

data class ParticipantResponse(val id: Int, val name: String, val score: Int)

data class GroupDetailResponse(
    val id: Int,
    val name: String,
    val invite_code: String,
    val participants: List<ParticipantResponse>,
    val next_matches: List<MatchResponse>
)

data class LeaderboardEntry(val position: Int, val id: Int, val name: String, val score: Int)

data class MatchResponse(
    val id: Int,
    val home_team: String,
    val away_team: String,
    val match_date: String,
    val phase: String,
    val group_name: String?,
    val status: String,
    val home_score: Int?,
    val away_score: Int?,
    val stadium: String?
)

data class PredictionRequest(val match_id: Int, val home_score: Int, val away_score: Int)

data class PredictionResponse(val message: String, val prediction: PredictionData)

data class PredictionData(
    val id: Int,
    val match_id: Int,
    val home_score: Int,
    val away_score: Int,
    val status: String
)

data class MyPredictionResponse(
    val id: Int,
    val match_id: Int,
    val home_score: Int,
    val away_score: Int,
    val points_earned: Int?,
    val status: String,
    val match: MatchResponse
)

data class StadiumResponse(
    val id: Int,
    val name: String,
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int
)

data class MatchesUpdateResponse(
    val synced_at: String,
    val matches: List<MatchResponse>
)

data class ErrorResponse(val message: String, val errors: Map<String, List<String>>?)

data class LogoutResponse(val message: String)
