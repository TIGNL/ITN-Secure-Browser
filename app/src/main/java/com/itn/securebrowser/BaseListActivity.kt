package com.itn.securebrowser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * الكلاس الأساسي لجميع صفحات القوائم الكاملة في التطبيق.
 *
 * يضمن:
 *   • هيكل موحد: Header 64dp + Divider + RecyclerView
 *   • زر الرجوع يغلق الصفحة تلقائياً
 *   • عنوان الصفحة قابل للتخصيص عبر setListTitle()
 *   • RecyclerView جاهز مع LinearLayoutManager عمودي
 *
 * الاستخدام:
 *   class MyActivity : BaseListActivity() {
 *       override fun onCreate(...) {
 *           super.onCreate(...)
 *           setListTitle("عنوان الصفحة")
 *           baseRecyclerView.adapter = MyAdapter(...)
 *       }
 *   }
 */
abstract class BaseListActivity : AppCompatActivity() {

    protected lateinit var baseRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_list)

        baseRecyclerView = findViewById(R.id.baseRecyclerView)
        baseRecyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<android.widget.ImageButton>(R.id.btnBack)
            .setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    /**
     * تعيين عنوان الصفحة في الـ Header.
     * استدعِها من onCreate في الكلاس الوارث.
     */
    protected fun setListTitle(title: String) {
        findViewById<android.widget.TextView>(R.id.listHeaderTitle).text = title
    }
}
