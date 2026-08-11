package com.itn.securebrowser.util;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

public class LocaleHelper {

    public static final String APP_LOCALE = "en";

    public static Context wrap(Context context) {
        Locale locale = new Locale(APP_LOCALE);
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}
