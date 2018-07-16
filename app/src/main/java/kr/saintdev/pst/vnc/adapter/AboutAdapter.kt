package kr.saintdev.pst.vnc.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kr.saintdev.pst.R

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-11
 */
class AboutAdapter : BaseAdapter() {
    private val items = arrayListOf<AboutItem>()

    fun addItems(array: ArrayList<AboutItem>) = items.addAll(array)

    fun addItem(obj : AboutItem) = items.add(obj)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =
                if(convertView == null) {
                    val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    inflater.inflate(R.layout.layout_about, parent, false)
                } else convertView

        val item = items[position]

        val titleView = view.findViewById<TextView>(R.id.layout_about_title)
        val contentView = view.findViewById<TextView>( R.id.layout_about_content)
        val iconView = view.findViewById<ImageView>(R.id.layout_about_icon)

        titleView.text = item.title
        contentView.text = item.content
        iconView.setImageResource(item.icon)
        view.tag = item.idx

        return view
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = items.size
}

data class AboutItem(val title: String, val content: String, val icon: Int, val idx: Int)