package com.example.buildingfexfrontend.support.data.remote

import com.example.buildingfexfrontend.support.domain.model.ChatMessage
import com.example.buildingfexfrontend.support.domain.model.SupportChat
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

data class ChatMessageDto(
    val id: String? = null,
    val authorRole: String? = null,
    val body: String? = null,
    val createdAt: String? = null,
    val authorName: String? = null,
) {
    fun toDomain() = ChatMessage(
        id = id.orEmpty(),
        authorRole = authorRole.orEmpty(),
        body = body.orEmpty(),
        createdAt = createdAt.orEmpty(),
        authorName = authorName,
    )

    fun from(domain: ChatMessage) = ChatMessageDto(
        id = domain.id,
        authorRole = domain.authorRole,
        body = domain.body,
        createdAt = domain.createdAt,
        authorName = domain.authorName,
    )
}

data class SupportChatDto(
    val id: String? = null,
    val ownerAdminId: String? = null,
    val residentId: String? = null,
    val residentName: String? = null,
    val topic: String? = null,
    val status: String? = null,
    val messages: List<ChatMessageDto>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    fun toDomain() = SupportChat(
        id = id.orEmpty(),
        ownerAdminId = ownerAdminId,
        residentId = residentId.orEmpty(),
        residentName = residentName.orEmpty(),
        topic = topic ?: "Soporte",
        status = status ?: "open",
        messages = messages.orEmpty().map { it.toDomain() },
        createdAt = createdAt.orEmpty(),
        updatedAt = updatedAt.orEmpty(),
    )

    fun from(domain: SupportChat) = SupportChatDto(
        id = domain.id,
        ownerAdminId = domain.ownerAdminId,
        residentId = domain.residentId,
        residentName = domain.residentName,
        topic = domain.topic,
        status = domain.status,
        messages = domain.messages.map { ChatMessageDto().from(it) },
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
    )
}

interface SupportApi {

    @GET("supportChats")
    suspend fun list(@QueryMap params: Map<String, @JvmSuppressWildcards String>): List<SupportChatDto>

    @GET("supportChats/{id}")
    suspend fun get(@Path("id") id: String): SupportChatDto

    @POST("supportChats")
    suspend fun create(@Body body: SupportChatDto): SupportChatDto

    @PUT("supportChats/{id}")
    suspend fun put(@Path("id") id: String, @Body body: SupportChatDto): SupportChatDto
}
