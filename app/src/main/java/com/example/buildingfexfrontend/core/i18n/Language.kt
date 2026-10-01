package com.example.buildingfexfrontend.core.i18n

/** Supported app languages (native names are used as labels in the UI). */
enum class Language(val tag: String) {
    ES("es"),
    EN("en"),
    ;

    companion object {
        fun fromTag(tag: String?): Language =
            entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: ES
    }
}
