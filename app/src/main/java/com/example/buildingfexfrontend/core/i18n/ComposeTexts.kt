package com.example.buildingfexfrontend.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Composable lookup: recomposes automatically when the language changes.
 * Use inside `Text(string("area.key"))` and any composable parameter.
 */
@Composable
fun string(key: String, fallback: String? = null): String {
    val language by AppLanguage.flow.collectAsState()
    return translate(key, language, fallback)
}
