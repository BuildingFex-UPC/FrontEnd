package com.example.buildingfexfrontend.core.i18n

/** (Spanish, English) texts for one translation key. */
typealias TextPair = Pair<String, String>

/**
 * Global flat catalog: every module contributes its own map from its own
 * `<Module>Texts.kt` file (each file is owned by one feature to avoid merge
 * conflicts). Keys are dot-separated and MUST be prefixed with the owning area
 * (e.g. "shell.nav.dashboard", "res.title").
 *
 * All catalog files are registered here once; editing this list is not needed when
 * adding keys to an existing catalog.
 */
private val mergedCatalog: Map<String, TextPair> by lazy {
    buildMap {
        putAll(ErrTexts)
        putAll(ComponentsTexts)
        putAll(ShellTexts)
        putAll(AuthTexts)
        putAll(DashboardTexts)
        putAll(ResidentsTexts)
        putAll(TeamTexts)
        putAll(ImportsTexts)
        putAll(InformationTexts)
        putAll(ServicesTexts)
        putAll(SpacesTexts)
        putAll(ReservationsTexts)
        putAll(GenerationTexts)
        putAll(IncidentsTexts)
        putAll(FinanceTexts)
        putAll(SettingsTexts)
        putAll(SupportTexts)
    }
}

/** Looks up [key] for [language]; returns [fallback] (or the key) when missing. */
fun translate(key: String, language: Language, fallback: String? = null): String =
    translateOrNull(key, language) ?: fallback ?: key

/** Like [translate] but returns null when the key is unknown (lets callers chain fallbacks). */
fun translateOrNull(key: String, language: Language): String? {
    val pair = mergedCatalog[key] ?: return null
    val text = if (language == Language.EN) pair.second else pair.first
    return text.ifBlank { null }
}

/** Lookup from non-composable code (ViewModels, domain) using the current language. */
fun stringOf(key: String, fallback: String? = null): String =
    translate(key, AppLanguage.current, fallback)
