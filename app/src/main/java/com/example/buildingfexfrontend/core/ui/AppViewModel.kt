package com.example.buildingfexfrontend.core.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Creates a [ViewModel] with an inline factory so screens can pull their
 * dependencies straight from the [com.example.buildingfexfrontend.core.di.AppContainer].
 */
@Composable
inline fun <reified T : ViewModel> appViewModel(noinline create: () -> T): T =
    viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    })
