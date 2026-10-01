package com.example.buildingfexfrontend.support.domain.repository

import com.example.buildingfexfrontend.support.domain.model.Faq
import com.example.buildingfexfrontend.support.domain.model.NewMessage
import com.example.buildingfexfrontend.support.domain.model.SupportChat

interface SupportRepository {
    fun faqs(): List<Faq>
    suspend fun listChatsForResident(residentId: String): List<SupportChat>
    suspend fun listChatsForAdmin(): List<SupportChat>
    suspend fun createChat(residentId: String, residentName: String, topic: String): SupportChat
    suspend fun getChat(id: String): SupportChat
    suspend fun appendMessage(chatId: String, message: NewMessage): SupportChat
}
