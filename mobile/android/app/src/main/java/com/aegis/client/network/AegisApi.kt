package com.aegis.client.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AegisApi {

    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @POST("escalate")
    suspend fun escalate(@Body request: ChatRequest): ChatResponse

    @Multipart
    @POST("analyze-logs")
    suspend fun analyzeLogs(
        @Part logFile: MultipartBody.Part,
        @Part("correlationId") correlationId: RequestBody?
    ): LogAnalysisResponse

    @GET("incidents/{correlationId}")
    suspend fun incidentTimeline(@Path("correlationId") correlationId: String): IncidentTimelineResponse

    @GET("incidents")
    suspend fun incidentTimelineByFilters(
        @Query("platform") platform: String? = null,
        @Query("eventType") eventType: String? = null,
        @Query("size") size: Int = 50
    ): IncidentTimelineResponse

    @GET("status/components")
    suspend fun componentStatus(): ComponentStatusResponse
}
