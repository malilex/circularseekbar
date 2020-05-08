package com.alex_malishev.circular_seekbar

import android.animation.ArgbEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlin.math.*

/**
 * @author Alexander Malishev
 */
class CircularSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), OnTouchListener {

    /**
     * Represents progress text format.
     * ProgressFormat.TIME represents progress in minutes and seconds;
     * ProgressFormat.PLAIN represents progress in raw values
     */
    enum class ProgressFormat {
        TIME,
        PLAIN
    }

    /**
     * Represents the type of direction
     */
    enum class Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE
    }


    /**
     * Listener for the CircularSeekBar
     */
    interface SeekListener {
        fun onProgressChanged(seekBar: CircularSeekBar, progress: Long, byUser: Boolean)
        fun onStartTrackingTouch(seekBar: CircularSeekBar)
        fun onStopTrackingTouch(seekBar: CircularSeekBar)
    }

    // Public variables
    /**
     * Represents the direction of seek
     */
    var seekDirection: Direction = Direction.CLOCKWISE
        set(value) {
            field = value
            refresh()

        }
    /**
     * Represents text format. @see [ProgressFormat]
     */
    var format = ProgressFormat.PLAIN
        set(value) {
            field = value
            refresh()
        }
    /**
     * The minimum value of progress
     */
    var minProgress: Long = 0
        set(value) {
            field = value
            refresh()
        }
    /**
     * The maximum value of progress
     */
    var maxProgress: Long = 0
        set(value) {
            field = value
            refresh()
        }
    /**
     * Represents the ability to handle touch events inside a circle
     */
    var detectTouchInside = false
        set(value) {
            field = value
            refresh()
        }
    /**
     * If true, the text representation of a progress in specified [ProgressFormat] will be drawn
     */
    var textProgressVisible = false
        set(value) {
            field = value
            refresh()
        }

    /**
     * The start angle of a seek bar. It must be between 0 and 360 inclusively.
     */
    var startAngle = 0f
        set(value) {
            field = value.coerceIn(0f, 360f)
            refresh()
        }
    /**
     * The end angle of a seek bar. It must be between [startAngle] and 360 inclusively. Also note, the
     * [endAngle] must be greater than the [startAngle].
     */
    var endAngle = 0f
        set(value) {
            field = value.coerceIn(startAngle, 360f)
            refresh()
        }

    /**
     * Represents current seek position
     */
    var progress: Long = 0
        set(value) {
            val isNew = value != field
            field = value.coerceIn(minProgress, maxProgress)
            if (!mUserTouchActive) {
                refresh()
            }
            if (isNew) seekListener?.onProgressChanged(this, field, mUserTouchActive)
        }

    /**
     * Represents current secondary progress.
     */
    var secondaryProgress: Long = 0
        set(value) {
            field = value.coerceIn(minProgress, maxProgress)
            refresh()
        }
    /**
     * The rotation angle of [CircularSeekBar]. It must be between 0 and 360 inclusively.
     * You should use it in combination with [startAngle] and [endAngle] to achieve desired position of
     * [CircularSeekBar]
     */
    var rotate = 0f
        set(value) {
            field = value.coerceIn(0f, 360f)
            refresh()
        }
    /**
     * The thumb icon. If value is null, then default icon will be used.
     */
    var thumb: Drawable? = null

    // Private variables
    private val mNormalThumbRadius: Float
    private val mPressedThumbRadius: Float
    private var mTextPaint: Paint
    private var mThumbRadius = 0f
    private var mThumbSelectionRadius = 0f
    private var mProgressPaint: Paint
    private var mStaticPaint: Paint
    private var mThumbPaint: Paint
    private var mThumbSelectionPaint: Paint
    private var mSecondaryProgressPaint: Paint
    private var mOvalRect = RectF()
    private var mCircleRadius = 0f
    private var mProgressSweepAngle = 0f
    private var mSecondaryProgressSweepAngle = 0f
    private var currentThumbX = 0f
    private var currentThumbY = 0f
    private var desiredWidth = 0
    private var desiredHeight = 0
    private var seekListener: SeekListener? = null
    private var mUserTouchActive = false
    private val mSweepAngle: Float
        get() = (endAngle - startAngle) * if (seekDirection == Direction.CLOCKWISE) 1 else -1

    @ColorInt
    private var mPrimaryColor = 0
    @ColorInt
    private var mSecondaryColor = 0
    @ColorInt
    private var mStaticColor = 0
    @ColorInt
    private var mTextColor = 0

    init {
        obtainStyleAttributes(context, attrs)
        sDP = resources.displayMetrics.density
        mProgressPaint = Paint().apply {
            strokeWidth = sDP * STROKE_WIDTH
            style = Paint.Style.STROKE
            color = mPrimaryColor
            alpha = 255
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        mSecondaryProgressPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = sDP * STROKE_WIDTH
            style = Paint.Style.STROKE
            color = mSecondaryColor
            strokeCap = Paint.Cap.ROUND
            alpha = 128
        }

        mStaticPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = sDP * STROKE_WIDTH
            style = Paint.Style.STROKE
            color = mStaticColor
            strokeCap = Paint.Cap.ROUND
            alpha = 255 / 3
        }

        mThumbPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = mPrimaryColor
        }

        mThumbSelectionPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.GRAY
            alpha = 50
        }

        mTextPaint = Paint().apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            style = Paint.Style.FILL
            color = mTextColor
            textSize = 18 * sDP
        }

        desiredWidth = (resources.displayMetrics.density * 200).toInt()
        desiredHeight = (resources.displayMetrics.density * 200).toInt()
        mPressedThumbRadius = sDP * PRESSED_THUMB_RADIUS
        mNormalThumbRadius = sDP * THUMB_RADIUS
        mThumbRadius = mNormalThumbRadius
        updateThumbPosition()
    }

    /**
     * The setter for [SeekListener]
     */
    fun setSeekListener(seekListener: SeekListener?) {
        this.seekListener = seekListener
    }

    /**
     * The setter for [thumb].
     * @param res: Int - the id of drawable resource
     */
    fun setThumb(@DrawableRes res: Int) {
        thumb = ContextCompat.getDrawable(context, res)
    }

    /**
     * Sets the color of primary progress of seekbar.
     * @param primaryColor: Int - the id of color resource
     * @param animate: Boolean - if true, then color will be changed with animation
     */
    fun setPrimaryColor(@ColorRes primaryColor: Int, animate: Boolean = true) {
        val toColor = ContextCompat.getColor(context, primaryColor)
        if (animate) {
            mProgressPaint.animateColorChange(toColor)
            mThumbPaint.animateColorChange(toColor)
        } else {
            this.mPrimaryColor = toColor
            mProgressPaint.color = toColor
            mThumbPaint.color = toColor
            invalidate()
        }
    }

    /**
     * Sets the color of secondary progress of seekbar.
     * @param secondaryColor: Int - the id of color resource
     * @param animate: Boolean - if true, then color will be changed with animation
     */
    fun setSecondaryColor(@ColorRes secondaryColor: Int, animate: Boolean = true) {
        val toColor = ContextCompat.getColor(context, secondaryColor)
        if (animate) {
            mSecondaryProgressPaint.animateColorChange(toColor)
        } else {
            mSecondaryColor = toColor
            mSecondaryProgressPaint.color = toColor
            invalidate()
        }
    }

    /**
     * Sets the color of text progress of seekbar.
     * @param textColor: Int - the id of color resource
     * @param animate: Boolean - if true, then color will be changed with animation
     */
    fun setTextColor(@ColorRes textColor: Int, animate: Boolean = true) {
        val toColor = ContextCompat.getColor(context, textColor)
        if (animate) {
            mTextPaint.animateColorChange(toColor)
        } else {
            mTextColor = toColor
            mTextPaint.color = toColor
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnTouchListener(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mOvalRect = if (w >= h) {
            val top = SELECTION_SCALE * mPressedThumbRadius
            val bottom = h - SELECTION_SCALE * mPressedThumbRadius
            mCircleRadius = (bottom - top) / 2
            val left = w / 2 - mCircleRadius
            val right = w / 2 + mCircleRadius
            RectF(left, top, right, bottom)
        } else {
            val left = SELECTION_SCALE * mPressedThumbRadius
            val right = w - SELECTION_SCALE * mPressedThumbRadius
            mCircleRadius = (right - left) / 2
            val top = h / 2 - mCircleRadius
            val bottom = h / 2 + mCircleRadius
            RectF(left, top, right, bottom)
        }
        updateThumbPosition()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.rotate(rotate, mOvalRect.centerX(), mOvalRect.centerY())
        canvas.drawArc(
            mOvalRect,
            if (seekDirection == Direction.CLOCKWISE) startAngle else -startAngle,
            mSweepAngle,
            false,
            mStaticPaint
        )
        canvas.drawArc(
            mOvalRect,
            if (seekDirection == Direction.CLOCKWISE) startAngle else -startAngle,
            mSecondaryProgressSweepAngle,
            false,
            mSecondaryProgressPaint
        )
        canvas.drawArc(
            mOvalRect,
            if (seekDirection == Direction.CLOCKWISE) startAngle else -startAngle,
            mProgressSweepAngle,
            false,
            mProgressPaint
        )
        drawThumb(canvas)
        drawThumbSelection(canvas)
        canvas.restore()
        if (textProgressVisible) drawTextProgress(canvas)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val x = motionEvent.x - mOvalRect.centerX()
        val y = motionEvent.y - mOvalRect.centerY()
        return when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchRadius = sqrt(x * x + y * y)
                val shouldIgnoreTouch = !detectTouchInside
                        && touchRadius < mCircleRadius - INSIDE_TOUCH_OFFSET * sDP
                        || touchRadius > mCircleRadius + INSIDE_TOUCH_OFFSET * sDP
                if (shouldIgnoreTouch) return false
                seekListener?.onStartTrackingTouch(this)
                detectTouchAngle(x, y)
                animateThumbScale(mPressedThumbRadius, true)
                mUserTouchActive = true
                true
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                detectTouchAngle(x, y)
                true
            }
            MotionEvent.ACTION_UP -> {
                animateThumbScale(mNormalThumbRadius, false)
                seekListener?.onStopTrackingTouch(this)
                mUserTouchActive = false
                true
            }
            else -> false
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState()).apply {
            progress = this@CircularSeekBar.progress
            secondaryProgress = this@CircularSeekBar.secondaryProgress
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? SavedState)?.let {
            super.onRestoreInstanceState(it.superState)
            this.progress = it.progress
            this.secondaryProgress = it.secondaryProgress
        } ?: super.onRestoreInstanceState(state)
    }

    private fun obtainStyleAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CircularSeekBar,
                0, 0
            )
            maxProgress = a.getInteger(
                R.styleable.CircularSeekBar_maxValue,
                DEFAULT_MAX_PROGRESS
            ).toLong()

            minProgress = a.getInteger(
                R.styleable.CircularSeekBar_minValue,
                DEFAULT_MIN_PROGRESS
            ).toLong()

            startAngle = a.getFloat(
                R.styleable.CircularSeekBar_startAngle,
                DEFAULT_START_ANGLE
            )

            endAngle = a.getFloat(
                R.styleable.CircularSeekBar_endAngle,
                DEFAULT_END_ANGLE
            )

            mPrimaryColor = a.getColor(
                R.styleable.CircularSeekBar_primaryColor,
                DEFAULT_PROGRESS_COLOR
            )

            mSecondaryColor = a.getColor(
                R.styleable.CircularSeekBar_secondaryColor,
                DEFAULT_SECONDARY_PROGRESS_COLOR
            )

            mStaticColor = a.getColor(
                R.styleable.CircularSeekBar_staticColor,
                DEFAULT_STATIC_COLOR
            )

            mTextColor = a.getColor(
                R.styleable.CircularSeekBar_textColor,
                mPrimaryColor
            )

            format = ProgressFormat.values()[a.getInt(
                R.styleable.CircularSeekBar_textFormat,
                1
            )]

            thumb = a.getDrawable(R.styleable.CircularSeekBar_thumb)
            detectTouchInside = a.getBoolean(R.styleable.CircularSeekBar_detectTouchInside, false)
            textProgressVisible =
                a.getBoolean(R.styleable.CircularSeekBar_textProgressEnabled, false)
            rotate = a.getFloat(R.styleable.CircularSeekBar_rotateAngle, 0f)
            seekDirection =
                Direction.values()[a.getInt(R.styleable.CircularSeekBar_seekDirection, 0)]
            progress = a.getInteger(R.styleable.CircularSeekBar_progress, 0).toLong()
            secondaryProgress =
                a.getInteger(R.styleable.CircularSeekBar_secondaryProgress, 0).toLong()
        } else {
            startAngle = DEFAULT_START_ANGLE
            endAngle = DEFAULT_END_ANGLE
            mPrimaryColor = DEFAULT_PROGRESS_COLOR
            mStaticColor = DEFAULT_STATIC_COLOR
            mSecondaryColor = DEFAULT_SECONDARY_PROGRESS_COLOR
            maxProgress = DEFAULT_MAX_PROGRESS.toLong()
            minProgress = DEFAULT_MIN_PROGRESS.toLong()
            detectTouchInside = false
            format = ProgressFormat.PLAIN
            seekDirection = Direction.CLOCKWISE
            thumb = null
            rotate = 0f
        }
    }

    private fun detectTouchAngle(x: Float, y: Float) {
        mProgressSweepAngle = if (seekDirection == Direction.CLOCKWISE)
            detectClockwise(x, y)
        else
            detectCounterClockwise(x, y)
        updateThumbPosition()
        calculateProgress()
    }

    private fun detectClockwise(x: Float, y: Float): Float {
        var touchAngle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat() - rotate
        if (touchAngle < 0) touchAngle += 360
        val start = startAngle
        val end = endAngle
        return when {
            touchAngle in (start..end + 1) -> touchAngle - start
            mProgressSweepAngle > mSweepAngle / 2 -> mSweepAngle
            else -> 0f
        }
    }

    private fun detectCounterClockwise(x: Float, y: Float): Float {
        var touchAngle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat() - rotate
        if (touchAngle > 0) touchAngle -= 360
        val start = -endAngle
        val end = -startAngle
        return when {
            touchAngle in (start..end + 1) -> touchAngle - end
            mProgressSweepAngle.absoluteValue > mSweepAngle.absoluteValue / 2 -> mSweepAngle
            else -> 0f
        }
    }

    private fun calculateProgress() {
        val newProgress = (mProgressSweepAngle * (maxProgress - minProgress) / mSweepAngle).toInt()
        progress = minProgress + newProgress
    }

    private fun calculateProgressAngle() {
        mProgressSweepAngle = progress * mSweepAngle / (maxProgress - minProgress)
    }

    private fun calculateSecondaryProgressAngle() {
        mSecondaryProgressSweepAngle =
            secondaryProgress * mSweepAngle / (maxProgress - minProgress)
    }

    private fun updateThumbPosition() {
        val deltaX = mOvalRect.centerX()
        val deltaY = mOvalRect.centerY()
        val koef = if (seekDirection == Direction.CLOCKWISE) 1 else -1
        currentThumbX =
            (mCircleRadius * cos(Math.toRadians((koef * startAngle + mProgressSweepAngle).toDouble())) + deltaX).toFloat()
        currentThumbY =
            (mCircleRadius * sin(Math.toRadians((koef * startAngle + mProgressSweepAngle).toDouble())) + deltaY).toFloat()
        this.invalidate()
    }

    private fun drawTextProgress(canvas: Canvas) {
        val text = when (format) {
            ProgressFormat.TIME -> DateUtils.formatElapsedTime(progress / 1000)
            else -> progress.toString()
        }
        canvas.drawText(
            text,
            mOvalRect.centerX(),
            mOvalRect.centerY() + mTextPaint.textSize / 2,
            mTextPaint
        )
    }

    private fun drawThumb(canvas: Canvas) {
        thumb?.let {
            val left = (currentThumbX - mThumbRadius).toInt()
            val right = (currentThumbX + mThumbRadius).toInt()
            val top = (currentThumbY - mThumbRadius).toInt()
            val bottom = (currentThumbY + mThumbRadius).toInt()
            it.setBounds(left, top, right, bottom)
            canvas.save()
            canvas.rotate(-rotate, currentThumbX, currentThumbY)
            it.draw(canvas)
            canvas.restore()
        } ?: canvas.drawCircle(currentThumbX, currentThumbY, mThumbRadius, mThumbPaint)
    }

    private fun drawThumbSelection(canvas: Canvas) {
        if (mThumbSelectionRadius > 0) {
            canvas.drawCircle(
                currentThumbX,
                currentThumbY,
                mThumbSelectionRadius,
                mThumbSelectionPaint
            )
        }
    }

    private fun animateThumbScale(toRadius: Float, isSelected: Boolean) {
        val thumbRadius = PropertyValuesHolder.ofFloat(
            PROPERTY_THUMB_RADIUS,
            mThumbRadius,
            toRadius
        )
        val thumbSelectionRadius = PropertyValuesHolder.ofFloat(
            PROPERTY_SELECTION_RADIUS,
            mThumbSelectionRadius,
            if (isSelected) SELECTION_SCALE * toRadius else 0f
        )
        ValueAnimator.ofPropertyValuesHolder(thumbRadius, thumbSelectionRadius).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                mThumbRadius = it.getAnimatedValue(PROPERTY_THUMB_RADIUS) as Float
                mThumbSelectionRadius = it.getAnimatedValue(PROPERTY_SELECTION_RADIUS) as Float
                invalidate()
            }
            start()
        }
    }

    private fun Paint.animateColorChange(@ColorInt toColor: Int) {
        ValueAnimator.ofInt(color, toColor).apply {
            setEvaluator(ArgbEvaluator())
            addUpdateListener {
                color = it.animatedValue as Int
            }
            duration = 150
            start()
        }
    }

    private fun refresh() {
        calculateProgressAngle()
        calculateSecondaryProgressAngle()
        updateThumbPosition()
    }

    companion object {
        private const val STROKE_WIDTH = 3f
        private val DEFAULT_PROGRESS_COLOR = Color.parseColor("#FF4081")
        private val DEFAULT_SECONDARY_PROGRESS_COLOR = Color.parseColor("#ff4081")
        private const val DEFAULT_STATIC_COLOR = Color.GRAY
        private const val DEFAULT_START_ANGLE = 180f
        private const val DEFAULT_END_ANGLE = 360f
        private const val THUMB_RADIUS = 6f
        private const val SELECTION_SCALE = 1.5f
        private const val PRESSED_THUMB_RADIUS = 9f
        private const val DEFAULT_MAX_PROGRESS = 100
        private const val DEFAULT_MIN_PROGRESS = 0
        private const val INSIDE_TOUCH_OFFSET = 10f
        private const val PROPERTY_SELECTION_RADIUS = "thumbSelectionRadius"
        private const val PROPERTY_THUMB_RADIUS = "thumbRadius"
        private var sDP = 0f
    }

    private class SavedState : BaseSavedState {
        var progress = 0L
        var secondaryProgress = 0L

        internal constructor(superState: Parcelable?) : super(superState) {}

        private constructor(inP: Parcel) : super(inP) {
            progress = inP.readLong()
            secondaryProgress = inP.readLong()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeLong(progress)
            out.writeLong(secondaryProgress)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SavedState?> = object : Parcelable.Creator<SavedState?> {
            override fun createFromParcel(`in`: Parcel): SavedState? {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}