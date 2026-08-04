package com.itn.securebrowser

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.os.Bundle

class ParentalSettingsActivity : BaseListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageTitle(getString(R.string.parental_settings_title))

        addItem(getString(R.string.tab_sites))  { openPin { startActivity(Intent(this, SiteListActivity::class.java)) } }
        addItem(getString(R.string.tab_groups)) { openPin { startActivity(Intent(this, GroupListActivity::class.java)) } }
        addItem(getString(R.string.tab_pin))    { startActivity(Intent(this, PinEntryActivity::class.java)) }
    }

    private fun addItem(label: String, onClick: () -> Unit) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_settings_row, listContainer, false)
        view.findViewById<TextView>(R.id.rowLabel).text = label
        view.setOnClickListener { onClick() }
        listContainer.addView(view)
    }

    private fun openPin(onVerified: () -> Unit) {
        // إن كان PIN مفعلاً يطلب التحقق أولاً وإلا يفتح مباشرة
        if (PinManager(this).hasPin()) {
            PinEntryActivity.startForResult(this, onVerified)
        } else {
            onVerified()
        }
    }
}
