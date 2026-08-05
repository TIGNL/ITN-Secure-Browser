package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupsFragment : BaseListSheet() {

    private lateinit var blockDataStore: BlockDataStore
    private lateinit var emptyState: LinearLayout
    private lateinit var groupsList: RecyclerView
    private lateinit var btnAddGroup: ImageButton
    private lateinit var adapter: GroupAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPageTitle(getString(R.string.tab_groups))

        val contentView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_groups, listContainer, false)

        blockDataStore = BlockDataStore(requireContext())

        emptyState  = contentView.findViewById(R.id.emptyState)
        groupsList  = contentView.findViewById(R.id.groupsList)
        btnAddGroup = contentView.findViewById(R.id.btnAddGroup)

        adapter = GroupAdapter(
            groups      = emptyList(),
            onItemClick = { group -> GroupEditSheet.newInstance(group.name).show(parentFragmentManager, "group_edit") },
            onDelete    = { group -> confirmDelete(group) }
        )
        groupsList.layoutManager = LinearLayoutManager(requireContext())
        groupsList.adapter = adapter

        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_divider)!!)
        groupsList.addItemDecoration(divider)

        btnAddGroup.setOnClickListener {
            GroupEditSheet.newInstance(null).show(parentFragmentManager, "group_edit")
        }

        showView(contentView)
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
        DeleteGroupSheet(group.name) {
            blockDataStore.deleteGroup(group.name)
            refresh()
        }.show(parentFragmentManager, "delete_group")
    }
}
