package com.example.buildingfexfrontend

import android.app.Application
import android.content.Context
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.AppLanguage
import com.example.buildingfexfrontend.core.i18n.Language

class BuildingFexApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("buildingfex_settings", Context.MODE_PRIVATE)
        AppLanguage.onChange = { language ->
            prefs.edit().putString("language", language.tag).apply()
        }
        AppLanguage.set(Language.fromTag(prefs.getString("language", null)))
        container = AppContainer(this)
    }

    companion object {
        fun from(context: Context): BuildingFexApp =
            context.applicationContext as BuildingFexApp
    }
}
