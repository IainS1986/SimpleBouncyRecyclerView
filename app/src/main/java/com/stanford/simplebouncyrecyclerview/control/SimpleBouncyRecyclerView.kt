package com.stanford.simplebouncyrecyclerview.control

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanford.simplebouncyrecyclerview.R

enum class BouncyState { UP, DOWN }

// This is the anim duration time to bounce back and it multiplied by the strength
private const val _animDuration: Int = 300

class SimpleBouncyRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAtr: Int = 0) : RecyclerView(context, attrs, defStyleAtr) {

    private var _layoutManager: SimpleBouncyLayoutManager
    private var _itemDecoration: SimpleBouncyOverscrollItemDecoration

    init {
        _layoutManager = SimpleBouncyLayoutManager(context, attrs, 0, 0)
        _layoutManager.onOverscroll = {
            if (it) {
                invalidate()
            }
        }
        layoutManager = _layoutManager

        _itemDecoration = SimpleBouncyOverscrollItemDecoration(context, attrs, _layoutManager)
        addItemDecoration(_itemDecoration)

        overScrollMode = View.OVER_SCROLL_NEVER
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

    // This is the max overscroll amount which is based on the screen size and multiplied by the tension
    private var _maxOverscroll: Double = 0.0

    private var _currentState: BouncyState = BouncyState.UP

    private var _bounceInterpolator: TimeInterpolator = DecelerateInterpolator()

    private var _bounceBackAnimator: ValueAnimator? = null

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

    private var _tension: Float = 1.0f
    var tension: Float = 1.0f
        get() = _tension
        set(value) {
            field = value
        }

    private var _strength: Float = 1.0f
    var strength: Float = 1.0f
        get() = _strength
        set(value) {
            field = value
        }

    val isVertical: Boolean
        get() = orientation == 1

    var onOverscroll: (animating: Boolean) -> Unit = {}

    init {
        val displayMetrics = DisplayMetrics()
        val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        _maxOverscroll = if (isVertical) displayMetrics.heightPixels.toDouble() / 4 else displayMetrics.widthPixels.toDouble() / 3

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.bouncy_scroller)

            tension = a.getFloat(R.styleable.bouncy_scroller_tension, tension)
            strength = a.getFloat(R.styleable.bouncy_scroller_strength, strength)
            startIndexOffset = a.getInt(R.styleable.bouncy_scroller_startIndexOffset, startIndexOffset)
            endIndexOffset = a.getInt(R.styleable.bouncy_scroller_endIndexOffset, endIndexOffset)

            a.recycle()
        }
    }

    override fun supportsPredictiveItemAnimations(): Boolean = false

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return handleScroll(dy, recycler, state)
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return handleScroll(dx, recycler, state)
    }

    fun setState(state: BouncyState) {
        if (_currentState == BouncyState.DOWN &&
                state == BouncyState.UP) {
            bounceBack()
        }
        else if (_currentState == BouncyState.UP &&
                state == BouncyState.DOWN) {
            clearAnimations()
        }
        _currentState = state
    }

    private fun handleScroll(delta: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        var toScroll = delta

        // If we are currently overscrolling, and we drag in the opposite direction, reduce
        // overscroll instead of scrolling until we are 'done'
        if (kotlin.math.abs(_overscrollTotal) > 0 &&
            ((toScroll > 0 && _overscrollTotal < 0) || (toScroll < 0 && _overscrollTotal > 0))){

            if (kotlin.math.abs(_overscrollTotal) >= kotlin.math.abs(toScroll)) {
                updateOverscroll(toScroll.toDouble())
                return 0
            }
            else if (kotlin.math.abs(toScroll) > kotlin.math.abs(_overscrollTotal)) {
                toScroll -= _overscrollTotal.toInt()
                reset()
                //Fallthrough to normal scroll handling
            }
        }

        var scrollRange: Int

        if (isVertical) {
            scrollRange = super.scrollVerticallyBy(toScroll, recycler, state)
        }
        else {
            scrollRange = super.scrollHorizontallyBy(toScroll, recycler, state)
        }

        val overscroll = toScroll - scrollRange;
        var dampen = if (_currentState == BouncyState.UP) 1.25 else 1.0 // Initial dampen, allow flings to fling further than a pull (over...fling the overscroll)
        dampen -= kotlin.math.abs(_overscrollTotal) / (_maxOverscroll * (1.0 / tension)) // Alter the base dampen with our MaxOverscroll and Tension values
        updateOverscroll(overscroll * dampen)

        return scrollRange
    }

    private fun updateOverscroll(overscroll: Double) {
        if (kotlin.math.abs(overscroll) < Double.MIN_VALUE) {
            return
        }

        _overscrollTotal += overscroll

        translateCells(false)

        // Bounce back immediately if touchstate is up (fling)
        if (_currentState == BouncyState.UP) {
            bounceBack()
        }
    }

    private fun translateCells(animating: Boolean) {
        if (_overscrollTotal > 0) {
            overscrollEnd()
        }
        else {
            overcrollStart()
        }

        // Fire event to notify anything that cares that the cells
        // have just translated
        onOverscroll(animating)
    }

    private fun translateCell(index: Int) {
        var view = safeGetChildAt(index)
        if (view != null) {
            if (isVertical) {
                view.translationY = -_overscrollTotal.toFloat()
            }
            else {
                view.translationX = -_overscrollTotal.toFloat()
            }
        }
    }

    private fun bounceBack() {
        clearAnimations()

        _bounceBackAnimator = ValueAnimator.ofFloat(_overscrollTotal.toFloat(), 0f)
        _bounceBackAnimator!!.interpolator = _bounceInterpolator
        _bounceBackAnimator!!.duration = (_animDuration * (1.0f / strength)).toLong()
        _bounceBackAnimator!!.addUpdateListener { bounceBackUpdate(it.animatedValue as Float) }
        _bounceBackAnimator!!.addListener(onEnd = { bounceBackEnded() }, onCancel = { bounceBackEnded() })
        _bounceBackAnimator!!.start()

    }

    private fun bounceBackUpdate(animatedValue: Float) {
        _overscrollTotal = animatedValue.toDouble()
        translateCells(true)
    }

    private fun bounceBackEnded() {
        reset()
    }

    private fun overcrollStart() {
        for ( i in startIndexOffset until childCount) {
            translateCell(i)
        }
    }

    private fun overscrollEnd() {
        for ( i in (childCount - endIndexOffset - 1) downTo 0) {
            translateCell(i)
        }
    }

    private fun reset() {
        for ( i in 0 until childCount) {
            val view = safeGetChildAt(i)
            if (view != null) {
                if (isVertical) {
                    view.translationY = 0f
                }
                else {
                    view.translationX = 0f
                }
            }
        }

        clearAnimations()

        _overscrollTotal = 0.0
    }

    private fun clearAnimations() {
        if (_bounceBackAnimator != null) {
            _bounceBackAnimator!!.removeAllUpdateListeners()
            _bounceBackAnimator!!.removeAllListeners()
            _bounceBackAnimator!!.cancel()
            _bounceBackAnimator = null
        }
    }

    private fun safeGetChildAt(index: Int): View? {
        if (index in 0 until childCount) {
            return getChildAt(index)
        }

        return null
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