package com.itn.securebrowser

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * All activities extend this class so the app locale is applied
 * consistently everywhere. To change the app language, update
 * LocaleHelper.APP_LOCALE and rebuild.
 */
open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }
}
