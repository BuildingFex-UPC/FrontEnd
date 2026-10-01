package com.example.buildingfexfrontend.support.domain.model

data class Faq(val id: Int, val question: String, val answer: String)

data class ChatMessage(
    val id: String,
    val authorRole: String,
    val body: String,
    val createdAt: String,
    val authorName: String? = null,
)

data class SupportChat(
    val id: String,
    val ownerAdminId: String? = null,
    val residentId: String,
    val residentName: String = "",
    val topic: String = "Soporte",
    val status: String = "open",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class NewMessage(
    val authorRole: String,
    val body: String,
    val authorName: String? = null,
)

/**
 * Static FAQ catalog. Question/answer hold i18n keys (`sup.faq.*`) resolved at
 * the presentation layer; the web app renders the same entries via i18n keys.
 */
object FaqCatalog {
    val entries: List<Faq> = listOf(
        Faq(1, "sup.faq.1.q", "sup.faq.1.a"),
        Faq(2, "sup.faq.2.q", "sup.faq.2.a"),
        Faq(3, "sup.faq.3.q", "sup.faq.3.a"),
        Faq(4, "sup.faq.4.q", "sup.faq.4.a"),
    )
}
