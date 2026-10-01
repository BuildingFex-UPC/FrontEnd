package com.example.buildingfexfrontend.subscription.domain.model

/** Plan identifiers stored from Settings (same as the web `SubscriptionPlanId`). */
enum class SubscriptionPlan(
    val id: String,
    val label: String,
    val residentLimit: Int,
    val monthlyPricePen: Int,
) {
    FREE("free", "Free", 9, 0),
    ESSENTIAL("essential", "Essential", 15, 40),
    STANDARD("standard", "Standard", 40, 80),
    SCALE("scale", "Scale", 80, 120);

    companion object {
        fun fromId(id: String?): SubscriptionPlan =
            entries.firstOrNull { it.id == id } ?: FREE
    }
}

data class Subscription(
    val planId: String = SubscriptionPlan.FREE.id,
    val residentLimit: Int = SubscriptionPlan.FREE.residentLimit,
    val residentsCount: Int = 0,
    val monthlyPricePen: Int = 0,
    val paidUntil: String? = null,
    val isPaid: Boolean = false,
) {
    val plan: SubscriptionPlan get() = SubscriptionPlan.fromId(planId)
}

data class CheckoutSession(
    val preferenceId: String?,
    val initPoint: String?,
    val demo: Boolean,
    val amount: Double,
    val planId: String,
)

data class ConfirmResult(
    val activated: Boolean,
    val planId: String,
    val paidUntil: String?,
    val subscription: Subscription?,
)
