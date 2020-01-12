package com.stanford.simplebouncyrecyclerview.control

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanford.simplebouncyrecyclerview.R

enum class BouncyState { UP, DOWN }

class SimpleBouncyRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAtr: Int = 0) : RecyclerView(context, attrs, defStyleAtr) {

    private var _layoutManager: SimpleBouncyLayoutManager
    private var _itemDecoration: SimpleBouncyOverscrollItemDecoration

    init {
        _layoutManager = SimpleBouncyLayoutManager(context, attrs, 0, 0)
//        _layoutManager.onOverscroll = {
//            if (animated) {
//                invalidate()
//            }
//        }

        _itemDecoration = SimpleBouncyOverscrollItemDecoration(context, attrs, _layoutManager)

        addItemDecoration(_itemDecoration)
        layoutManager = _layoutManager
    }

    var startIndexOffset: Int
        get() = _layoutManager.startIndexOffset
        set(value) {
            _layoutManager.startIndexOffset = value
        }

    var endIndexOffset: Int
        get() = _layoutManager.endIndexOffset
        set(value) {
            _layoutManager.endIndexOffset = value
        }

    var startOverscrollColor: Int
        get() = _itemDecoration.startOverscrollColor
        set(value) {
            _itemDecoration.startOverscrollColor = value
        }

    var endOverscrollColor: Int
        get() = _itemDecoration.endOverscrollColor
        set(value) {
            _itemDecoration.endOverscrollColor = value
        }

    override fun onTouchEvent(e: MotionEvent?): Boolean {

        when (e?.action) {
            MotionEvent.ACTION_UP -> _layoutManager.setState(BouncyState.UP)
            MotionEvent.ACTION_CANCEL -> _layoutManager.setState(BouncyState.UP)
            MotionEvent.ACTION_DOWN -> _layoutManager.setState(BouncyState.DOWN)
            MotionEvent.ACTION_MOVE -> _layoutManager.setState(BouncyState.DOWN)
        }

        return super.onTouchEvent(e)
    }
}

class SimpleBouncyLayoutManager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAtr: Int = 0,
    defStyleRes: Int = 0) : LinearLayoutManager(context, attrs, defStyleAtr, defStyleRes) {

    private var _overscrollTotal: Double = 0.0
    val overscrollTotal: Double
        get() = _overscrollTotal

    private var _startIndexOffset: Int = 0
    var startIndexOffset: Int = 0
        get() = _startIndexOffset
        set(value) {
            field = value
        }

    private var _endIndexOffset: Int = 0
    var endIndexOffset: Int = 0
        get() = _endIndexOffset
        set(value) {
            field = value
        }

    val isVertical: Boolean
        get() = orientation == 1

    fun setState(state: BouncyState) {

    }
}

class SimpleBouncyOverscrollItemDecoration(context: Context?, attrs: AttributeSet?, layoutManager: SimpleBouncyLayoutManager) :
    DividerItemDecoration(context, layoutManager.orientation) {

    private var _paint: Paint = Paint()

    private var _layoutManager: SimpleBouncyLayoutManager = layoutManager

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

        // Figure out the Width/Height to expand by
        if (_layoutManager.isVertical) {
            y = if (start) child.y- child.translationY else child.y+ child.measuredHeight
            h = kotlin.math.abs(child.translationY)
        } else {
            x = if (start) child.x - child.translationX else child.x + child.measuredWidth
            w = kotlin.math.abs(child.translationX)
        }

        c.drawRect(x, y, x + w, y + h, _paint)
    }
}