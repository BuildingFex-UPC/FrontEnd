package com.example.buildingfexfrontend.socialspaces.application

/**
 * Absolute URL shared with guests. While the hosted web app has no domain,
 * the link points to the backend's public guest-invite endpoint (anonymous
 * `GET /reservations?guestInviteToken=...`), so scanning the QR always opens
 * the reservation details. Set [WEB_ORIGIN] once the SPA is deployed and the
 * link switches to its `/invite/<token>` page automatically.
 */
object GuestInviteLink {

    /** Origin of the hosted BuildingFex web app (no trailing slash). */
    const val WEB_ORIGIN: String = ""

    fun build(token: String): String {
        val clean = token.trim()
        if (clean.isEmpty()) return ""
        val encoded = java.net.URLEncoder.encode(clean, "UTF-8")
        val origin = WEB_ORIGIN
        return if (origin.isNotBlank()) {
            origin.trimEnd('/') + "/invite/" + encoded
        } else {
            com.example.buildingfexfrontend.BuildConfig.API_BASE_URL.trimEnd('/') +
                "/reservations?guestInviteToken=$encoded"
        }
    }
}
