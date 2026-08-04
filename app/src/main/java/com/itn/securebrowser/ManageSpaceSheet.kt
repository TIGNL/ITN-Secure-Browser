package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class ManageSpaceSheet : BaseBottomSheet() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_manage_space, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text =
            getString(R.string.manage_space_title)

        if (PinManager.hasPin(requireContext())) {
            PinEntrySheet(
                mode = PinEntrySheet.MODE_VERIFY,
                subtitle = getString(R.string.pin_subtitle_manage_data),
                onPinVerified = { showOptions() }
            ).show(parentFragmentManager, "pin")
        } else {
            showOptions()
        }
    }

    private fun showOptions() {
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(getString(R.string.manage_space_title))
            .setItems(
                arrayOf(
                    getString(R.string.clear_browsing),
                    getString(R.string.clear_tracking),
                    getString(R.string.clear_blocking),
                    getString(R.string.clear_all)
                )
            ) { _, which ->
                when (which) {
                    0 -> clearBrowsing()
                    1 -> clearTracking()
                    2 -> clearBlocking()
                    3 -> confirmClearAll()
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { _, _ -> dismiss() }
            .setOnCancelListener { dismiss() }
            .show()
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
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(getString(R.string.clear_all_title))
            .setMessage(getString(R.string.clear_all_message))
            .setPositiveButton(getString(R.string.btn_clear_all_confirm)) { _, _ ->
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                WebStorage.getInstance().deleteAllData()
                requireContext().cacheDir.deleteRecursively()
                requireContext().getSharedPreferences("itn_time_tracker", 0).edit().clear().apply()
                requireContext().getSharedPreferences("itn_block_data", 0).edit().clear().apply()
                PinManager.clear(requireContext())
                toast(getString(R.string.toast_cleared_all))
                dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { _, _ -> dismiss() }
            .show()
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
