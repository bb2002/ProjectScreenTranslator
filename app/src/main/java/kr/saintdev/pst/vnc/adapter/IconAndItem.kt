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
class BillLogAdapter : BaseAdapter() {
    val items = arrayListOf<BillLogItem>()

    fun setItems(array: ArrayList<BillLogItem>) {
        this.items.clear()
        this.items.addAll(array)
    }

    fun addItem(item: BillLogItem) {
        this.items.add(item)
    }

    fun clear() {
        this.items.clear()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if(convertView == null) {
            val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.layout_billing, parent, false)
        } else convertView

        // 객체 찾기
        val titleView = view.findViewById<TextView>(R.id.billlog_title)
        val iconView = view.findViewById<ImageView>(R.id.billlog_icon)
        val contentView = view.findViewById<TextView>(R.id.billlog_content)

        val item = this.items[position]

        titleView.text = item.title
        contentView.text = item.content
        iconView.setImageResource(item.icon)

        return view
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = items.size
}

data class BillLogItem(val title: String, val content: String, val icon: Int)