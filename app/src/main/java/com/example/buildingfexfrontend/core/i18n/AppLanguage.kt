package com.example.buildingfexfrontend.core.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide language holder (pure Kotlin so domain code can use it).
 * Persistence is wired from the Application via [onChange].
 */
object AppLanguage {

    private val _flow = MutableStateFlow(Language.ES)
    val flow: StateFlow<Language> = _flow.asStateFlow()

    val current: Language get() = _flow.value

    /** Invoked after every change so the Android layer can persist the choice. */
    var onChange: ((Language) -> Unit)? = null

    fun set(language: Language) {
        if (_flow.value == language) return
        _flow.value = language
        onChange?.invoke(language)
    }

    fun toggle() = set(if (current == Language.ES) Language.EN else Language.ES)
}
