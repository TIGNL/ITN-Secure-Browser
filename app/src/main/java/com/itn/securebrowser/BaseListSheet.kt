package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

open class BaseListSheet : BaseBottomSheet() {

    protected lateinit var listContainer: LinearLayout
    private lateinit var listScrollView: View
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_base_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listContainer     = view.findViewById(R.id.listContainer)
        listScrollView    = view.findViewById(R.id.listScrollView)
        fragmentContainer = view.findViewById(R.id.fragmentContainer)

        view.findViewById<View>(R.id.btnBack).setOnClickListener { dismiss() }
    }

    protected fun setPageTitle(title: String) {
        view?.findViewById<TextView>(R.id.headerTitle)?.text = title
    }

    protected fun addListItem(
        @DrawableRes iconRes: Int,
        iconTint: Int = requireContext().getColor(R.color.accent),
        label: String,
        onClick: () -> Unit
    ) {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_list_row, listContainer, false)

        row.findViewById<ImageView>(R.id.itemIcon).apply {
            setImageResource(iconRes)
            setColorFilter(iconTint)
        }
        row.findViewById<TextView>(R.id.itemLabel).text = label
        row.setOnClickListener { onClick() }

        listContainer.addView(row)
    }

    protected fun showFragment(fragment: Fragment) {
        listScrollView.visibility    = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
