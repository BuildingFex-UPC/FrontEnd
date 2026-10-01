package com.example.buildingfexfrontend.subscription.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

data class SubscriptionDto(
    @SerializedName(value = "planId", alternate = ["PlanId"])
    val planId: String? = null,
    @SerializedName(value = "residentLimit", alternate = ["ResidentLimit"])
    val residentLimit: Int? = null,
    @SerializedName(value = "residentsCount", alternate = ["ResidentsCount"])
    val residentsCount: Int? = null,
    @SerializedName(value = "monthlyPricePen", alternate = ["MonthlyPricePen"])
    val monthlyPricePen: Int? = null,
    @SerializedName(value = "paidUntil", alternate = ["PaidUntil"])
    val paidUntil: String? = null,
    @SerializedName(value = "isPaid", alternate = ["IsPaid"])
    val isPaid: Boolean? = null,
)

data class PlanChangeBody(val planId: String)

data class CheckoutBody(val planId: String)

data class CheckoutDto(
    val preferenceId: String? = null,
    val initPoint: String? = null,
    val demo: Boolean? = null,
    val amount: Double? = null,
    val planId: String? = null,
)

data class ConfirmBody(
    val planId: String,
    val paymentId: Long? = null,
    val demo: Boolean = false,
)

data class ConfirmDto(
    val activated: Boolean? = null,
    val planId: String? = null,
    val paidUntil: String? = null,
    val subscription: SubscriptionDto? = null,
)

/** Subscription (plan) endpoints of the shared bounded context. */
interface SubscriptionApi {

    @GET("api/v1/subscription")
    suspend fun getCurrent(): SubscriptionDto

    @PATCH("api/v1/subscription")
    suspend fun changePlan(@Body body: PlanChangeBody): SubscriptionDto

    @POST("api/v1/subscription/checkout")
    suspend fun checkout(@Body body: CheckoutBody): CheckoutDto

    @POST("api/v1/subscription/confirm")
    suspend fun confirm(@Body body: ConfirmBody): ConfirmDto
}
