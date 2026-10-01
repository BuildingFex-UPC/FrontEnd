package com.example.buildingfexfrontend.core.i18n

/**
 * ES/EN texts for "Mis reservas" and guest invitations (`resv.*`).
 * Feature-owned by socialspaces/presentation (MyReservationsScreen, MyReservationsViewModel).
 */
val ReservationsTexts: Map<String, TextPair> = mapOf(
    "resv.title" to ("Mis reservas" to "My reservations"),
    "resv.subtitle" to ("Administra los invitados de tus reservas y comparte la invitación." to "Manage the guests of your reservations and share the invitation."),
    "resv.empty" to ("No tienes reservas todavía. Solicita una desde Espacios comunes." to "You have no reservations yet. Request one from Common areas."),
    "resv.spaceFallback" to ("Espacio" to "Space"),
    "resv.guests" to ("Invitados" to "Guests"),
    "resv.hide" to ("Ocultar" to "Hide"),
    "resv.editGuests" to ("Editar invitados" to "Edit guests"),
    "resv.noGuests" to ("Sin invitados registrados." to "No guests registered."),
    "resv.checkedIn" to ("Ingresó" to "Checked in"),
    "resv.notCheckedIn" to ("Sin ingresar" to "Not checked in"),
    "resv.inviteTitle" to ("Invitación de huéspedes" to "Guest invitation"),
    "resv.inviteExpired" to ("La invitación expiró. Edita los invitados para generarla de nuevo." to "The invitation expired. Edit the guests to generate it again."),
    "resv.inviteQrDesc" to ("Código QR de la invitación" to "QR code of the invitation"),
    "resv.shareQr" to ("Compartir QR" to "Share QR"),
    "resv.save" to ("Guardar" to "Save"),
    "resv.guestsHint" to ("Hasta {n} nombres. Los que queden en blanco no se guardan." to "Up to {n} names. Blank ones are not saved."),
    "resv.guestLabel" to ("Invitado {n}" to "Guest {n}"),
    "resv.understood" to ("Entendido" to "Got it"),
    "resv.shareChooser" to ("Compartir invitación" to "Share invitation"),
    "resv.guestsUpdated" to ("Invitados actualizados." to "Guests updated."),
    "resv.shareUrl" to ("BuildingFex · invitación de huéspedes: {url}" to "BuildingFex · guest invitation: {url}"),
    "resv.shareToken" to ("BuildingFex · invitación de huéspedes (token): {token}" to "BuildingFex · guest invitation (token): {token}"),
)
