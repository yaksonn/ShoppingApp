package com.yaksonn.shoppingapp.utils

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.math.min

class CustomDividerItemDecorator constructor(private val divider: Drawable?)  : RecyclerView.ItemDecoration() {



    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount = Objects.requireNonNull(parent.adapter)?.let {
            min(
                parent.childCount, it.itemCount
            )
        }
        if (childCount != null) {
            for (i in 0 until childCount - 1) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val dividerTop = child.bottom + params.bottomMargin
                val dividerBottom = dividerTop + divider!!.intrinsicHeight
                divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                divider.draw(canvas)
            }
        }
    }
}