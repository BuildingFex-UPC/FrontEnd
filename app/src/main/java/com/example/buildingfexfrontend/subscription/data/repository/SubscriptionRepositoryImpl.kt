package com.example.buildingfexfrontend.subscription.data.repository

import com.example.buildingfexfrontend.core.data.network.apiCall
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.subscription.data.remote.CheckoutBody
import com.example.buildingfexfrontend.subscription.data.remote.ConfirmBody
import com.example.buildingfexfrontend.subscription.data.remote.PlanChangeBody
import com.example.buildingfexfrontend.subscription.data.remote.SubscriptionApi
import com.example.buildingfexfrontend.subscription.data.remote.SubscriptionDto
import com.example.buildingfexfrontend.subscription.domain.model.CheckoutSession
import com.example.buildingfexfrontend.subscription.domain.model.ConfirmResult
import com.example.buildingfexfrontend.subscription.domain.model.Subscription
import com.example.buildingfexfrontend.subscription.domain.repository.SubscriptionRepository

class SubscriptionRepositoryImpl(
    private val api: SubscriptionApi,
    @Suppress("unused") private val session: SessionRepository,
) : SubscriptionRepository {

    override suspend fun current(): Subscription = apiCall { api.getCurrent().toDomain() }

    override suspend fun changeToPlan(planId: String): Subscription = apiCall {
        api.changePlan(PlanChangeBody(planId)).toDomain()
    }

    override suspend fun checkout(planId: String): CheckoutSession = apiCall {
        val dto = api.checkout(CheckoutBody(planId))
        CheckoutSession(
            preferenceId = dto.preferenceId,
            initPoint = dto.initPoint,
            demo = dto.demo == true,
            amount = dto.amount ?: 0.0,
            planId = dto.planId ?: planId,
        )
    }

    override suspend fun confirm(planId: String, paymentId: String?, demo: Boolean): ConfirmResult =
        apiCall {
            val dto = api.confirm(ConfirmBody(planId, paymentId?.toLongOrNull(), demo))
            ConfirmResult(
                activated = dto.activated == true,
                planId = dto.planId ?: planId,
                paidUntil = dto.paidUntil,
                subscription = dto.subscription?.toDomain(),
            )
        }

    private fun SubscriptionDto.toDomain(): Subscription = Subscription(
        planId = planId ?: "free",
        residentLimit = residentLimit ?: 9,
        residentsCount = residentsCount ?: 0,
        monthlyPricePen = monthlyPricePen ?: 0,
        paidUntil = paidUntil,
        isPaid = isPaid == true,
    )
}
