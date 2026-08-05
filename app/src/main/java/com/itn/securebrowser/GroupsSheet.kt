package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class GroupsSheet : BaseListSheet() {

    private lateinit var blockDataStore: BlockDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPageTitle(getString(R.string.tab_groups))
        blockDataStore = BlockDataStore(requireContext())
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        listContainer.removeAllViews()
        val groups = blockDataStore.getGroups()

        if (groups.isEmpty()) {
            val emptyHint = TextView(requireContext()).apply {
                text = getString(R.string.groups_empty_hint)
                setTextColor(requireContext().getColor(R.color.text_hint))
                textSize = 14f
                setPadding(48, 64, 48, 64)
                gravity = android.view.Gravity.CENTER
            }
            listContainer.addView(emptyHint)
        } else {
            groups.forEach { group -> addGroupItem(group) }
        }

        addListItem(R.drawable.ic_add, label = getString(R.string.btn_add_group)) {
            GroupEditSheet.newInstance(null).show(parentFragmentManager, "group_edit")
        }
    }

    private fun addGroupItem(group: BlockGroup) {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_group, listContainer, false)

        row.findViewById<TextView>(R.id.groupName).text = group.name
        row.findViewById<TextView>(R.id.groupDomains).text = when (group.domains.size) {
            0    -> "No domains"
            1    -> group.domains[0]
            2    -> "${group.domains[0]}, ${group.domains[1]}"
            else -> "${group.domains[0]}, ${group.domains[1]} +${group.domains.size - 2}"
        }

        row.findViewById<View>(R.id.btnDeleteGroup).setOnClickListener {
            confirmDelete(group)
        }

        row.setOnClickListener {
            GroupEditSheet.newInstance(group.name).show(parentFragmentManager, "group_edit")
        }

        listContainer.addView(row)
    }

    private fun confirmDelete(group: BlockGroup) {
        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
            .setTitle(getString(R.string.delete_group_title))
            .setMessage(getString(R.string.delete_group_message, group.name))
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                blockDataStore.deleteGroup(group.name)
                refresh()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
}
