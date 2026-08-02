package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroupsFragment : Fragment() {

    private lateinit var blockDataStore: BlockDataStore
    private lateinit var emptyState: LinearLayout
    private lateinit var groupsList: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_groups, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        blockDataStore = BlockDataStore(requireContext())

        emptyState = view.findViewById(R.id.emptyState)
        groupsList = view.findViewById(R.id.groupsList)
        fabAdd     = view.findViewById(R.id.fabAddGroup)

        adapter = GroupAdapter(
            groups     = emptyList(),
            onItemClick = { group -> GroupEditActivity.startEdit(requireContext(), group.name) },
            onDelete    = { group -> confirmDelete(group) }
        )
        groupsList.layoutManager = LinearLayoutManager(requireContext())
        groupsList.adapter = adapter

        fabAdd.setOnClickListener {
            GroupEditActivity.startNew(requireContext())
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
            .setTitle("حذف مجموعة")
            .setMessage("هل تريد حذف مجموعة «${group.name}»؟\nسيتم حذف جميع إعداداتها.")
            .setPositiveButton("حذف") { _, _ ->
                blockDataStore.deleteGroup(group.name)
                refresh()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
}
