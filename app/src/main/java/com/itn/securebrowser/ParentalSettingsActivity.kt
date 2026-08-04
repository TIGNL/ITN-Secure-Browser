package com.itn.securebrowser

import android.os.Bundle

class ParentalSettingsActivity : BaseListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageTitle(getString(R.string.parental_settings))

        addListItem(
            iconRes  = R.drawable.ic_sites,
            label    = getString(R.string.tab_sites),
            subtitle = getString(R.string.sites_empty_hint)
        ) {
            showFragment(SitesFragment())
        }

        addListItem(
            iconRes  = R.drawable.ic_groups,
            label    = getString(R.string.tab_groups),
            subtitle = getString(R.string.groups_empty_hint)
        ) {
            showFragment(GroupsFragment())
        }

        addListItem(
            iconRes  = R.drawable.ic_pin,
            label    = getString(R.string.tab_pin),
            subtitle = getString(R.string.pin_subtitle_open_settings)
        ) {
            showFragment(PinFragment())
        }
    }
}
