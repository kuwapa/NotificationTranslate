package com.pedalrhythm.notificationtranslate

import android.graphics.Rect
import android.view.View

import androidx.recyclerview.widget.RecyclerView

//From http://stackoverflow.com/a/27037230/3090120
class VerticalSpaceItemDecoration : RecyclerView.ItemDecoration {

    private val verticalSpaceHeight: Int
    private var isBottomToTop = false

    constructor(verticalSpaceHeight: Int) {
        this.verticalSpaceHeight = verticalSpaceHeight
    }

    constructor(verticalSpaceHeight: Int, isLaidBottomToTop: Boolean) {
        this.verticalSpaceHeight = verticalSpaceHeight
        this.isBottomToTop = isLaidBottomToTop
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
            if (isBottomToTop)
                outRect.top = verticalSpaceHeight
            else
                outRect.bottom = verticalSpaceHeight
        }
    }
}