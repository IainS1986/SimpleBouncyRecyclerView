package com.stanford.simplebouncyrecyclerview.control.layoutmanagers

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanford.simplebouncyrecyclerview.R
import com.stanford.simplebouncyrecyclerview.control.BouncyState

// This is the anim duration time to bounce back and it multiplied by the strength
private const val _animDuration: Int = 300

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

            _tension = a.getFloat(R.styleable.bouncy_scroller_tension, _tension)
            _strength = a.getFloat(R.styleable.bouncy_scroller_strength, _strength)
            _startIndexOffset = a.getInt(R.styleable.bouncy_scroller_startIndexOffset, _startIndexOffset)
            _endIndexOffset = a.getInt(R.styleable.bouncy_scroller_endIndexOffset, _endIndexOffset)

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
        for ( i in _startIndexOffset until childCount) {
            translateCell(i)
        }
    }

    private fun overscrollEnd() {
        for ( i in (childCount - _endIndexOffset - 1) downTo 0) {
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