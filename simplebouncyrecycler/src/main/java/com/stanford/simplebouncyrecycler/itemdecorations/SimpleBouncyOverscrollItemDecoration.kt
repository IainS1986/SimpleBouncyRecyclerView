package com.stanford.simplebouncyrecycler.itemdecorations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.stanford.simplebouncyrecycler.R
import com.stanford.simplebouncyrecycler.layoutmanagers.SimpleBouncyLayoutManager

internal class SimpleBouncyOverscrollItemDecoration(context: Context?, attrs: AttributeSet?, layoutManager: SimpleBouncyLayoutManager) :
    DividerItemDecoration(context, layoutManager.orientation) {

    private var _paint: Paint = Paint()

    private var _layoutManager: SimpleBouncyLayoutManager = layoutManager

    private var _context: Context? = context

    var startOverscrollColor: Int = Color.TRANSPARENT

    var endOverscrollColor: Int = Color.TRANSPARENT

    init {
        if (attrs != null) {
            val a = context!!.obtainStyledAttributes(attrs, R.styleable.bouncy_scroller)

            startOverscrollColor = a.getColor(R.styleable.bouncy_scroller_startOverscrollColor, startOverscrollColor)
            endOverscrollColor = a.getColor(R.styleable.bouncy_scroller_endOverscrollColor, endOverscrollColor)

            a.recycle()
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        if (kotlin.math.abs(_layoutManager.overscrollTotal) > Double.MIN_VALUE)
        {
            if (_layoutManager.overscrollTotal < 0 && startOverscrollColor != Color.TRANSPARENT)
            {
                // Get the child we render after
                var childAfter = parent.getChildAt(_layoutManager.startIndexOffset)
                // Render region after
                _paint.color = startOverscrollColor
                drawOverscrollRegion(c, childAfter, true)
            }
            else if (_layoutManager.overscrollTotal > 0 && endOverscrollColor != Color.TRANSPARENT)
            {
                // Get the child we render before
                var childBefore = parent.getChildAt(parent.childCount - _layoutManager.endIndexOffset - 1)
                // Render region
                _paint.color = endOverscrollColor
                drawOverscrollRegion(c, childBefore, false)

            }
        }
    }

    private fun drawOverscrollRegion(c: Canvas?, child: View?, start: Boolean){

        if (child == null || c == null) {
            return
        }

        // Set baseline to be the canvase size, we will then resize based on orientation, start/end and child position
        var x = 0f
        var y = 0f
        var w = c.width.toFloat()
        var h = c.height.toFloat()

        var overscrollAmount = kotlin.math.abs(_layoutManager.overscrollTotal)

        // FUDGE : RecyclerViews are putting a single DP gap between cells so overscroll region just needs
        // increading slightly
        var fudge = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, _context!!.resources.displayMetrics)

        // Figure out the Width/Height to expand by
        if (_layoutManager.isVertical) {
            y = if (start) child.y - child.translationY - child.marginTop - fudge else child.y + child.measuredHeight + child.marginBottom + fudge
            h = overscrollAmount.toFloat() + fudge
        } else {
            x = if (start) child.x - child.translationX - child.marginLeft - fudge else child.x + child.measuredWidth + child.marginRight + fudge
            w = overscrollAmount.toFloat() + fudge
        }

        c.drawRect(x, y, x + w, y + h, _paint)
    }
}