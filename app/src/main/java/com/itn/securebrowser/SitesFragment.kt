package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SitesFragment : Fragment() {

    private lateinit var blockDataStore: BlockDataStore
    private lateinit var emptyState: LinearLayout
    private lateinit var sitesList: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: SiteAdapter

    // أسماء أيام الأسبوع المستخدمة في BlockDataStore
    private val allDays = listOf(
        "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY",
        "WEDNESDAY", "THURSDAY", "FRIDAY"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_sites, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        blockDataStore = BlockDataStore(requireContext())

        emptyState = view.findViewById(R.id.emptyState)
        sitesList  = view.findViewById(R.id.sitesList)
        fabAdd     = view.findViewById(R.id.fabAddSite)

        // إعداد RecyclerView
        adapter = SiteAdapter(emptyList(), { site -> SiteSchedulesActivity.start(requireContext(), site.domain) }) { site -> confirmDelete(site) }
        sitesList.layoutManager = LinearLayoutManager(requireContext())
        sitesList.adapter = adapter

        fabAdd.setOnClickListener { showAddDialog() }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    // ── تحديث القائمة ────────────────────────────────────────────────────────

    private fun refresh() {
        val sites = blockDataStore.getSites()
        adapter.updateSites(sites)

        if (sites.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            sitesList.visibility  = View.GONE
        } else {
            emptyState.visibility = View.GONE
            sitesList.visibility  = View.VISIBLE
        }
    }

    // ── نافذة الإضافة ────────────────────────────────────────────────────────

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_site, null)

        val inputDomain = dialogView.findViewById<EditText>(R.id.inputDomain)
        val inputLimit  = dialogView.findViewById<EditText>(R.id.inputLimit)

        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(getString(R.string.add_site_title))
            .setView(dialogView)
            .setPositiveButton("إضافة") { _, _ ->
                handleAddSite(
                    rawDomain = inputDomain.text.toString().trim(),
                    rawLimit  = inputLimit.text.toString().trim()
                )
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun handleAddSite(rawDomain: String, rawLimit: String) {
        // ── تحقق من النطاق ─────────────────────────────────────────────────
        if (rawDomain.isBlank()) {
            toast(getString(R.string.err_enter_domain))
            return
        }

        // تنظيف: إزالة البروتوكول و www.
        val domain = rawDomain
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .trimEnd('/')
            .lowercase()

        if (domain.isBlank() || !domain.contains('.')) {
            toast(getString(R.string.err_invalid_domain))
            return
        }

        // ── تحقق من الحد اليومي ────────────────────────────────────────────
        val limitMins: Int? = if (rawLimit.isBlank()) {
            null      // بلا حد
        } else {
            val n = rawLimit.toIntOrNull()
            if (n == null || n <= 0) {
                toast(getString(R.string.err_daily_limit_positive))
                return
            }
            n
        }

        val dailyLimits: Map<String, Int> = if (limitMins != null) {
            allDays.associateWith { limitMins }
        } else {
            emptyMap()
        }

        // ── حفظ ────────────────────────────────────────────────────────────
        try {
            blockDataStore.saveSite(
                BlockSite(
                    domain      = domain,
                    dailyLimits = dailyLimits,
                    schedules   = emptyList()   // الجداول تأتي في ب-٤-ج
                )
            )
            refresh()
        } catch (e: IllegalStateException) {
            toast(e.message ?: getString(R.string.err_save))
        }
    }

    // ── حذف موقع ─────────────────────────────────────────────────────────────

    private fun confirmDelete(site: BlockSite) {
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(getString(R.string.delete_site_title))
            .setMessage(getString(R.string.delete_site_message, site.domain))
            .setPositiveButton("حذف") { _, _ ->
                blockDataStore.deleteSite(site.domain)
                refresh()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    // ── مساعدات ──────────────────────────────────────────────────────────────

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
