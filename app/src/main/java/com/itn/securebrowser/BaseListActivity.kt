package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

open class BaseListActivity : BaseActivity() {

    protected lateinit var listContainer: LinearLayout
    private lateinit var listScrollView: View
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_list)

        listContainer      = findViewById(R.id.listContainer)
        listScrollView     = findViewById(R.id.listScrollView)
        fragmentContainer  = findViewById(R.id.fragmentContainer)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    protected fun setPageTitle(title: String) {
        findViewById<TextView>(R.id.headerTitle).text = title
    }

    protected fun addListItem(
        @DrawableRes iconRes: Int,
        iconTint: Int = getColor(R.color.accent),
        label: String,
        onClick: () -> Unit
    ) {
        val row = LayoutInflater.from(this)
            .inflate(R.layout.item_list_row, listContainer, false)

        row.findViewById<ImageView>(R.id.itemIcon).apply {
            setImageResource(iconRes)
            setColorFilter(iconTint)
        }
        row.findViewById<TextView>(R.id.itemLabel).text = label
        row.setOnClickListener { onClick() }

        listContainer.addView(row)
    }

    protected fun showFragment(fragment: Fragment) {
        listScrollView.visibility     = View.GONE
        fragmentContainer.visibility  = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (fragmentContainer.visibility == View.VISIBLE) {
            fragmentContainer.visibility  = View.GONE
            listScrollView.visibility     = View.VISIBLE
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
