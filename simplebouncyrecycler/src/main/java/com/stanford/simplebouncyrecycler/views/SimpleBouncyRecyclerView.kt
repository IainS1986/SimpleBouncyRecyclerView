package com.stanford.simplebouncyrecycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.stanford.simplebouncyrecycler.BouncyState
import com.stanford.simplebouncyrecycler.itemdecorations.SimpleBouncyOverscrollItemDecoration
import com.stanford.simplebouncyrecycler.layoutmanagers.SimpleBouncyLayoutManager

typealias OverscrollEvent = (animating: Boolean) -> Unit

class SimpleBouncyRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAtr: Int = 0) : RecyclerView(context, attrs, defStyleAtr) {

    private var _layoutManager: SimpleBouncyLayoutManager

    private var _itemDecoration: SimpleBouncyOverscrollItemDecoration

    init {
        _layoutManager =
            SimpleBouncyLayoutManager(
                context,
                attrs,
                0,
                0
            )
        _layoutManager.registerOnOverscrollEvent {
            if (it) {
                invalidate()
            }
        }
        layoutManager = _layoutManager

        _itemDecoration =
            SimpleBouncyOverscrollItemDecoration(
                context,
                attrs,
                _layoutManager
            )
        addItemDecoration(_itemDecoration)

        overScrollMode = View.OVER_SCROLL_NEVER
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

    var tension: Float
        get() = _layoutManager.tension
        set(value) {
            _layoutManager.tension = value
        }

    var friction: Float
        get() = _layoutManager.friction
        set(value) {
            _layoutManager.friction = value
        }

    fun registerOnOverscrollEvent(event: OverscrollEvent) {
        _layoutManager.registerOnOverscrollEvent(event)
    }

    fun unregisterOnOverscrollEvent(event: OverscrollEvent) {
        _layoutManager.unregisterOnOverscrollEvent(event)
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