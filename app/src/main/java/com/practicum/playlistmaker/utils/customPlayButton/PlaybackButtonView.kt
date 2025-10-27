package com.practicum.playlistmaker.utils.customPlayButton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import com.practicum.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var isPlaying: Boolean = false
    private var playIcon: Drawable? = null
    private var pauseIcon: Drawable? = null

    private val drawableBounds = Rect()

    private var clickListener: OnClickListener? = null

    init {
        setupAttributes(attrs, defStyleAttr, defStyleRes)
        setupClickListeners()
        setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setupAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            defStyleAttr,
            defStyleRes
        ) {
            val playIconRes = getResourceId(R.styleable.PlaybackButtonView_playIcon, 0)
            val pauseIconRes = getResourceId(R.styleable.PlaybackButtonView_pauseIcon, 0)

            if (playIconRes != 0) {
                playIcon = loadIcon(playIconRes)
            }

            if (pauseIconRes != 0) {
                pauseIcon = loadIcon(pauseIconRes)
            }
        }
    }

    private fun loadIcon(resourceId: Int): Drawable? {
        return try {
            ResourcesCompat.getDrawable(resources, resourceId, null)
        } catch (e: Exception) {
            null
        }
    }

    private fun setupClickListeners() {
        super.setOnClickListener {
            togglePlaybackState()
            clickListener?.onClick(this)
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        clickListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val size = minOf(w, h)
        val left = (w - size) / 2
        val top = (h - size) / 2

        drawableBounds.set(left, top, left + size, top + size)

        playIcon?.bounds = drawableBounds
        pauseIcon?.bounds = drawableBounds
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentIcon = if (isPlaying) pauseIcon else playIcon

        currentIcon?.draw(canvas)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun togglePlaybackState() {
        isPlaying = !isPlaying
        invalidate()
    }

    fun setPlaying(playing: Boolean) {
        if (isPlaying != playing) {
            isPlaying = playing
            invalidate()
        }
    }
}