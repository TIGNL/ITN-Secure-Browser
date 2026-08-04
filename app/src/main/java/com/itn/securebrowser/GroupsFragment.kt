package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupsFragment : Fragment() {

    private lateinit var blockDataStore: BlockDataStore
    private lateinit var emptyState: LinearLayout
    private lateinit var groupsList: RecyclerView
    private lateinit var btnAddGroup: ImageButton
    private lateinit var adapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_groups, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        blockDataStore = BlockDataStore(requireContext())

        emptyState = view.findViewById(R.id.emptyState)
        groupsList = view.findViewById(R.id.groupsList)
        btnAddGroup = view.findViewById(R.id.btnAddGroup)

        adapter = GroupAdapter(
            groups     = emptyList(),
            onItemClick = { group -> GroupEditSheet.newInstance(group.name).show(requireActivity().supportFragmentManager, "group_edit") },
            onDelete    = { group -> confirmDelete(group) }
        )
        groupsList.layoutManager = LinearLayoutManager(requireContext())
        groupsList.adapter = adapter

        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_divider)!!)
        groupsList.addItemDecoration(divider)

        btnAddGroup.setOnClickListener {
            GroupEditSheet.newInstance(null).show(requireActivity().supportFragmentManager, "group_edit")
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val groups = blockDataStore.getGroups()
        adapter.updateGroups(groups)

        if (groups.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            groupsList.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            groupsList.visibility = View.VISIBLE
        }
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