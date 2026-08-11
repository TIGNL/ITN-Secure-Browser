package com.itn.securebrowser;

import android.content.Context;
import android.app.Activity;
import com.itn.securebrowser.util.LocaleHelper;

public class BaseActivity extends Activity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}
