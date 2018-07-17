package kr.saintdev.pst.vnc.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kr.saintdev.pst.R
import org.jetbrains.anko.backgroundColor

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-17
 */
class BillItemAdapter : BaseAdapter() {
    private val productItemArray = arrayListOf<ProductItemData>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val context = parent.context

        val view = if(convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.layout_item_grid, parent, false)
        } else convertView

        val itemTitle = view.findViewById<TextView>(R.id.product_item_title)
        val itemContent = view.findViewById<TextView>(R.id.product_item_subtitle)
        val itemPrice = view.findViewById<TextView>(R.id.product_item_price)

        val productItem = productItemArray[position]

        itemTitle.text = productItem.itemTitle
        itemContent.text = productItem.itemContent
        itemPrice.text = productItem.itemPrice

        view.backgroundColor = productItem.backgroundColor
        return view
    }

    override fun getItem(position: Int) = productItemArray[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = productItemArray.size

    fun addItem(item: ProductItemData) = this.productItemArray.add(item)

    fun addItemAll(vararg items: ProductItemData) = this.productItemArray.addAll(items)
}

data class ProductItemData(val itemTitle: String, val itemContent: String, val backgroundColor: Int, val itemPrice: String)