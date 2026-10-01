package com.example.buildingfexfrontend.subscription.application

import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.subscription.domain.model.CheckoutSession
import com.example.buildingfexfrontend.subscription.domain.model.ConfirmResult
import com.example.buildingfexfrontend.subscription.domain.model.Subscription
import com.example.buildingfexfrontend.subscription.domain.model.SubscriptionPlan
import com.example.buildingfexfrontend.subscription.domain.repository.SubscriptionRepository

class SubscriptionUseCases(
    private val repository: SubscriptionRepository,
    @Suppress("unused") private val session: SessionRepository,
) {
    val current = CurrentSubscriptionUseCase(repository)
    val residentLimit = ResidentLimitUseCase(repository)
    val changePlan = ChangePlanUseCase(repository)
    val checkout = CheckoutPlanUseCase(repository)
    val confirm = ConfirmPlanUseCase(repository)
}

class CurrentSubscriptionUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(): Subscription = repository.current()
}

class ResidentLimitUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(): Int = try {
        repository.current().residentLimit
    } catch (e: Exception) {
        SubscriptionPlan.FREE.residentLimit
    }
}

/**
 * Free downgrade. Paid plans go through [CheckoutPlanUseCase] +
 * [ConfirmPlanUseCase] (Mercado Pago), exactly like the web flow.
 */
class ChangePlanUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(planId: String): Subscription =
        repository.changeToPlan(planId)
}

class CheckoutPlanUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(planId: String): CheckoutSession = repository.checkout(planId)
}

class ConfirmPlanUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(
        planId: String,
        paymentId: String? = null,
        demo: Boolean = false,
    ): ConfirmResult = repository.confirm(planId, paymentId, demo)
}
