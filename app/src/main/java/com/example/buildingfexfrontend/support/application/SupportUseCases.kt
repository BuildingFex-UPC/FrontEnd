package com.example.buildingfexfrontend.support.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.support.domain.model.Faq
import com.example.buildingfexfrontend.support.domain.model.NewMessage
import com.example.buildingfexfrontend.support.domain.model.SupportChat
import com.example.buildingfexfrontend.support.domain.repository.SupportRepository

class SupportUseCases(
    private val repository: SupportRepository,
    private val session: SessionRepository,
) {
    val faqs = GetFaqsUseCase(repository)
    val myChats = ListMyChatsUseCase(repository, session)
    val allChats = ListAllChatsUseCase(repository)
    val startChat = StartChatUseCase(repository, session)
    val openChat = OpenChatUseCase(repository)
    val sendResidentMessage = SendMessageUseCase(repository, session, authorRole = "resident")
    val sendAdminMessage = SendMessageUseCase(repository, session, authorRole = "admin")
}

class GetFaqsUseCase(private val repository: SupportRepository) {
    operator fun invoke(): List<Faq> = repository.faqs()
}

class ListMyChatsUseCase(
    private val repository: SupportRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): List<SupportChat> {
        val residentId = session.current()?.profile?.id ?: return emptyList()
        return repository.listChatsForResident(residentId)
    }
}

class ListAllChatsUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(): List<SupportChat> = repository.listChatsForAdmin()
}

class StartChatUseCase(
    private val repository: SupportRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(topic: String): SupportChat {
        val profile = session.current()?.profile
            ?: throw AppException("RESIDENT_NOT_FOUND", "No resident session")
        return repository.createChat(
            residentId = profile.id,
            residentName = profile.name,
            topic = topic.ifBlank { "Soporte" },
        )
    }
}

class OpenChatUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(chatId: String): SupportChat = repository.getChat(chatId)
}

class SendMessageUseCase(
    private val repository: SupportRepository,
    private val session: SessionRepository,
    private val authorRole: String,
) {
    suspend operator fun invoke(chatId: String, body: String): SupportChat {
        val clean = body.trim()
        if (clean.isBlank()) {
            throw AppException("MESSAGE_EMPTY", "Escribe un mensaje.")
        }
        return repository.appendMessage(
            chatId = chatId,
            message = NewMessage(
                authorRole = authorRole,
                body = clean,
                authorName = session.current()?.profile?.name,
            ),
        )
    }
}
