package com.itn.securebrowser

import android.os.Bundle

class ParentalSettingsActivity : BaseListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageTitle(getString(R.string.parental_settings))

        addListItem(R.drawable.ic_sites, label = getString(R.string.tab_sites)) {
            showFragment(SitesFragment())
        }

        addListItem(R.drawable.ic_groups, label = getString(R.string.tab_groups)) {
            showFragment(GroupsFragment())
        }

        addListItem(R.drawable.ic_pin, label = getString(R.string.tab_pin)) {
            showFragment(PinFragment())
        }
    }
}
