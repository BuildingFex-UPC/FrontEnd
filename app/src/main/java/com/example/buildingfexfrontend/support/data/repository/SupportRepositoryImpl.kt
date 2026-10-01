package com.example.buildingfexfrontend.support.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.data.network.ownerParams
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.support.data.remote.SupportApi
import com.example.buildingfexfrontend.support.data.remote.SupportChatDto
import com.example.buildingfexfrontend.support.domain.model.Faq
import com.example.buildingfexfrontend.support.domain.model.NewMessage
import com.example.buildingfexfrontend.support.domain.model.SupportChat
import com.example.buildingfexfrontend.support.domain.repository.SupportRepository
import com.example.buildingfexfrontend.support.domain.model.FaqCatalog

class SupportRepositoryImpl(
    private val api: SupportApi,
    private val session: SessionRepository,
) : SupportRepository {

    override fun faqs(): List<Faq> = FaqCatalog.entries

    override suspend fun listChatsForResident(residentId: String): List<SupportChat> = apiCall {
        api.list(
            ownerParams(
                session,
                mapOf(
                    "residentId" to residentId,
                    "_sort" to "updatedAt",
                    "_order" to "desc",
                ),
            ),
        ).map { it.toDomain() }
    }

    override suspend fun listChatsForAdmin(): List<SupportChat> = apiCall {
        api.list(ownerParams(session, mapOf("_sort" to "updatedAt", "_order" to "desc")))
            .map { it.toDomain() }
    }

    override suspend fun createChat(
        residentId: String,
        residentName: String,
        topic: String,
    ): SupportChat = apiCall {
        val ownerId = session.activeDataOwnerId
            ?: throw AppException("OWNER_REQUIRED", "Owner admin required")
        val now = Dates.nowIso()
        api.create(
            SupportChatDto(
                id = "support-chat-${System.currentTimeMillis()}",
                ownerAdminId = ownerId,
                residentId = residentId,
                residentName = residentName,
                topic = topic.ifBlank { "Soporte" },
                status = "open",
                messages = emptyList(),
                createdAt = now,
                updatedAt = now,
            ),
        ).toDomain()
    }

    override suspend fun getChat(id: String): SupportChat = apiCall { api.get(id).toDomain() }

    /** Read-modify-write, mirroring the web `appendMessageInternal`. */
    override suspend fun appendMessage(chatId: String, message: NewMessage): SupportChat = apiCall {
        val chat = api.get(chatId).toDomain()
        val now = Dates.nowIso()
        val row = com.example.buildingfexfrontend.support.domain.model.ChatMessage(
            id = "msg-${System.currentTimeMillis()}-${(0..9999).random()}",
            authorRole = message.authorRole,
            body = message.body.trim(),
            createdAt = now,
            authorName = message.authorName,
        )
        val updated = chat.copy(messages = chat.messages + row, updatedAt = now)
        api.put(chatId, SupportChatDto().from(updated)).toDomain()
    }
}
