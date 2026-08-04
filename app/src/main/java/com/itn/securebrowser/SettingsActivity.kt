package com.itn.securebrowser

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsActivity : BaseActivity() {

    private lateinit var tabSites: TextView
    private lateinit var tabGroups: TextView
    private lateinit var tabPin: TextView
    private lateinit var settingsContent: FrameLayout

    private var currentTab = 0   // 0=sites, 1=groups, 2=pin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        tabSites        = findViewById(R.id.tabSites)
        tabGroups       = findViewById(R.id.tabGroups)
        tabPin          = findViewById(R.id.tabPin)
        settingsContent = findViewById(R.id.settingsContent)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        tabSites.setOnClickListener  { selectTab(0) }
        tabGroups.setOnClickListener { selectTab(1) }
        tabPin.setOnClickListener    { selectTab(2) }

        // Start on sites tab
        if (savedInstanceState == null) selectTab(0)
    }

    private fun selectTab(index: Int) {
        currentTab = index

        val tabs = listOf(tabSites, tabGroups, tabPin)
        val activeColor   = 0xFFFFFFFF.toInt()
        val inactiveColor = 0xFF777799.toInt()

        tabs.forEachIndexed { i, tab ->
            if (i == index) {
                tab.setTextColor(activeColor)
                tab.background = getDrawable(R.drawable.bg_tab_active_pill)
                tab.typeface = android.graphics.Typeface.DEFAULT_BOLD
            } else {
                tab.setTextColor(inactiveColor)
                tab.background = getDrawable(R.drawable.bg_tab_active_pill)
                tab.typeface = android.graphics.Typeface.DEFAULT
            }
        }

        // Swap fragment
        val fragment: Fragment = when (index) {
            0    -> SitesFragment()
            1    -> GroupsFragment()
            else -> PinFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.settingsContent, fragment)
            .commit()
    }
}
