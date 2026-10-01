package com.example.buildingfexfrontend.subscription.domain.repository

import com.example.buildingfexfrontend.subscription.domain.model.CheckoutSession
import com.example.buildingfexfrontend.subscription.domain.model.ConfirmResult
import com.example.buildingfexfrontend.subscription.domain.model.Subscription

interface SubscriptionRepository {

    suspend fun current(): Subscription

    suspend fun changeToPlan(planId: String): Subscription

    suspend fun checkout(planId: String): CheckoutSession

    suspend fun confirm(planId: String, paymentId: String?, demo: Boolean): ConfirmResult
}
