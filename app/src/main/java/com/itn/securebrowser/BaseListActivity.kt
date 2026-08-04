package com.itn.securebrowser

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

/**
 * كلاس أساسي لصفحات القائمة الموحدة.
 * كل صفحة ترث منه وتضيف عناصرها في listContainer.
 *
 * الاستخدام:
 *   class MyActivity : BaseListActivity() {
 *       override fun onCreate(...) {
 *           super.onCreate(...)
 *           setPageTitle("عنوان الصفحة")
 *           // أضف عناصر القائمة عبر listContainer
 *       }
 *   }
 */
open class BaseListActivity : BaseActivity() {

    protected lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_list)

        listContainer = findViewById(R.id.listContainer)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
    }

    /** تخصيص عنوان الشاشة */
    protected fun setPageTitle(title: String) {
        findViewById<TextView>(R.id.headerTitle).text = title
    }
}
