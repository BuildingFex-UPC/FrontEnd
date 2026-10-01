package com.example.buildingfexfrontend.core.data.network

import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Technical network wiring: one OkHttpClient (auth + 401 handling) and one
 * Retrofit instance shared by every bounded context. Each context declares its
 * own Retrofit service interface.
 */
class NetworkModule(
    baseUrl: String,
    private val session: SessionRepository,
    enableLogs: Boolean = false,
) {

    val gsonConverterFactory: GsonConverterFactory = GsonConverterFactory.create()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            var request = chain.request()
            val isAuthRoute = request.url.encodedPath.contains("/authentication/")
            val token = session.accessToken
            if (!isAuthRoute && !token.isNullOrBlank()) {
                request = request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }
            val response = chain.proceed(request)
            // Mirror the web interceptor: an expired session is cleared.
            if (response.code == 401 && !isAuthRoute) {
                session.clear()
            }
            response
        }
        .apply {
            if (enableLogs) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(normalizeBaseUrl(baseUrl))
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()

    inline fun <reified S> service(): S = retrofit.create(S::class.java)

    private companion object {
        fun normalizeBaseUrl(url: String): String {
            val trimmed = url.trim().trimEnd('/')
            return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        }
    }
}
