package com.itn.securebrowser

import android.os.Bundle
import android.view.View

class ParentalSettingsSheet : BaseListSheet() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPageTitle(getString(R.string.parental_settings))

        // PIN — يفتح PinSheet بدلاً من منطق مضمّن
        addListItem(R.drawable.ic_settings, label = getString(R.string.tab_pin)) {
            PinSheet().show(parentFragmentManager, "pin_sheet")
        }

        addListItem(R.drawable.ic_clock, label = getString(R.string.tab_groups)) {
            showFragment(GroupsSheet())
        }
    }
}
