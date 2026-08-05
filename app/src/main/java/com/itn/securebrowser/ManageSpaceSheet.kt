package com.itn.securebrowser

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.TextView
import android.widget.Toast

class ManageSpaceSheet(
    private val onDismissed: () -> Unit = {}
) : BaseBottomSheet() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_manage_space, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text =
            getString(R.string.manage_space_title)

        view.findViewById<View>(R.id.rowClearBrowsing).setOnClickListener { clearBrowsing() }
        view.findViewById<View>(R.id.rowClearTracking).setOnClickListener { clearTracking() }
        view.findViewById<View>(R.id.rowClearBlocking).setOnClickListener { clearBlocking() }
        view.findViewById<View>(R.id.rowClearAll).setOnClickListener { confirmClearAll() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissed()
    }

    private fun clearBrowsing() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        requireContext().cacheDir.deleteRecursively()
        toast(getString(R.string.toast_cleared_browsing))
        dismiss()
    }

    private fun clearTracking() {
        requireContext().getSharedPreferences("itn_time_tracker", 0).edit().clear().apply()
        toast(getString(R.string.toast_cleared_tracking))
        dismiss()
    }

    private fun clearBlocking() {
        requireContext().getSharedPreferences("itn_block_data", 0).edit().clear().apply()
        toast(getString(R.string.toast_cleared_blocking))
        dismiss()
    }

    private fun confirmClearAll() {
        ConfirmClearAllSheet {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
            requireContext().cacheDir.deleteRecursively()
            requireContext().getSharedPreferences("itn_time_tracker", 0).edit().clear().apply()
            requireContext().getSharedPreferences("itn_block_data", 0).edit().clear().apply()
            PinManager.clear(requireContext())
            toast(getString(R.string.toast_cleared_all))
            dismiss()
        }.show(parentFragmentManager, "confirm_clear_all")
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
