package com.itn.securebrowser

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    /**
     * Change this constant to switch the app language.
     * Examples: "en" for English, "ar" for Arabic, "fr" for French.
     */
    const val APP_LOCALE = "en"

    fun wrap(context: Context): Context {
        val locale = Locale(APP_LOCALE)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
