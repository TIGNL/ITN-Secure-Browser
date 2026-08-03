package com.itn.securebrowser

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsActivity : BaseActivity() {

    private lateinit var tabSites: TextView
    private lateinit var tabGroups: TextView
    private lateinit var tabPin: TextView
    private lateinit var tabIndicator: View
    private lateinit var settingsContent: FrameLayout

    private var currentTab = 0   // 0=sites, 1=groups, 2=pin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        tabSites        = findViewById(R.id.tabSites)
        tabGroups       = findViewById(R.id.tabGroups)
        tabPin          = findViewById(R.id.tabPin)
        tabIndicator    = findViewById(R.id.tabIndicator)
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

        // Update text colours
        val activeColour   = 0xFFFFFFFF.toInt()
        val inactiveColour = 0xFF888888.toInt()
        tabSites.setTextColor( if (index == 0) activeColour else inactiveColour)
        tabGroups.setTextColor(if (index == 1) activeColour else inactiveColour)
        tabPin.setTextColor(   if (index == 2) activeColour else inactiveColour)

        // Slide indicator
        tabIndicator.post {
            val tabWidth = tabSites.width
            val targetX  = (index * tabWidth).toFloat()
            val anim = ValueAnimator.ofFloat(tabIndicator.translationX, targetX)
            anim.duration = 200
            anim.addUpdateListener { tabIndicator.translationX = it.animatedValue as Float }
            anim.start()
            // Set indicator width = one tab
            tabIndicator.layoutParams = tabIndicator.layoutParams.also {
                it.width = tabWidth
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
