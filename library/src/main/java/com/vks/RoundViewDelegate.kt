package com.vks

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.View.LAYER_TYPE_SOFTWARE
import android.widget.TextView
import com.vks.R
import java.util.*


class RoundViewDelegate(private val view: View, private val context: Context, attrs: AttributeSet) {
    private val gdBackground = GradientDrawable()
    private val gdBackgroundPress = GradientDrawable()
    private var backgroundColor: Int = 0
    private var backgroundPressColor: Int = 0
    private var cornerRadius: Int = 0
    private var cornerRadiusTL: Int = 0
    private var cornerRadiusTR: Int = 0
    private var cornerRadiusBL: Int = 0
    private var cornerRadiusBR: Int = 0
    private var strokeWidth: Int = 0
    private var strokeColor: Int = 0
    private var strokePressColor: Int = 0
    private var textPressColor: Int = 0
    private var isRadiusHalfHeight: Boolean = false
    private var isRadiusHalfHeightLeft: Boolean = false
    private var isRadiusHalfHeightRight: Boolean = false
    private var isRadiusHalfWidthTop: Boolean = false
    private var isRadiusHalfWidthBottom: Boolean = false
    private var isWidthHeightEqual: Boolean = false
    private var isRippleEnable: Boolean = false
    private val radiusArr = FloatArray(8)
    private var dashWidth: Float = 0f
    private var dashGap: Float = 0f

    init {
        obtainAttributes(context, attrs)
    }

    private fun obtainAttributes(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RoundTextView)
        backgroundColor =
            ta.getColor(R.styleable.RoundTextView_rv_backgroundColor, Color.TRANSPARENT)
        backgroundPressColor =
            ta.getColor(R.styleable.RoundTextView_rv_backgroundPressColor, Integer.MAX_VALUE)
        cornerRadius = ta.getDimensionPixelSize(R.styleable.RoundTextView_rv_cornerRadius, 0)
        strokeWidth = ta.getDimensionPixelSize(R.styleable.RoundTextView_rv_strokeWidth, 0)
        strokeColor = ta.getColor(R.styleable.RoundTextView_rv_strokeColor, Color.TRANSPARENT)
        strokePressColor =
            ta.getColor(R.styleable.RoundTextView_rv_strokePressColor, Integer.MAX_VALUE)
        textPressColor = ta.getColor(R.styleable.RoundTextView_rv_textPressColor, Integer.MAX_VALUE)
        isRadiusHalfHeight = ta.getBoolean(R.styleable.RoundTextView_rv_isRadiusHalfHeight, false)
        isWidthHeightEqual = ta.getBoolean(R.styleable.RoundTextView_rv_isWidthHeightEqual, false)
        cornerRadiusTL = ta.getDimensionPixelSize(R.styleable.RoundTextView_rv_cornerRadius_TL, 0)
        cornerRadiusTR = ta.getDimensionPixelSize(R.styleable.RoundTextView_rv_cornerRadius_TR, 0)
        cornerRadiusBL = ta.getDimensionPixelSize(R.styleable.RoundTextView_rv_cornerRadius_BL, 0)
        cornerRadiusBR = ta.getDimensionPixelSize(R.styleable.RoundTextView_rv_cornerRadius_BR, 0)
        isRippleEnable = ta.getBoolean(R.styleable.RoundTextView_rv_isRippleEnable, true)
        isRadiusHalfHeightLeft =
            ta.getBoolean(R.styleable.RoundTextView_rv_isRadiusHalfHeightLeft, false)
        isRadiusHalfHeightRight =
            ta.getBoolean(R.styleable.RoundTextView_rv_isRadiusHalfHeightRight, false)
        isRadiusHalfWidthTop =
            ta.getBoolean(R.styleable.RoundTextView_rv_isRadiusHalfWidthTop, false)
        isRadiusHalfWidthBottom =
            ta.getBoolean(R.styleable.RoundTextView_rv_isRadiusHalfWidthBottom, false)
        dashWidth = ta.getDimension(R.styleable.RoundTextView_rv_dashWidth, 0f)
        dashGap = ta.getDimension(R.styleable.RoundTextView_rv_dashGap, 0f)
        ta.recycle()
    }

    fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        setBgSelector()
    }

    fun setBackgroundPressColor(backgroundPressColor: Int) {
        this.backgroundPressColor = backgroundPressColor
        setBgSelector()
    }

    fun setCornerRadius(cornerRadius: Int) {
        this.cornerRadius = dp2px(cornerRadius.toFloat())
        setBgSelector()
    }

    fun setCornerRadiusLeft(cornerRadius: Int) {
        this.cornerRadiusBL = dp2px(cornerRadius.toFloat())
        this.cornerRadiusTL = dp2px(cornerRadius.toFloat())
        setBgSelector()
    }

    fun setCornerRadiusRight(cornerRadius: Int) {
        this.cornerRadiusBR = dp2px(cornerRadius.toFloat())
        this.cornerRadiusTR = dp2px(cornerRadius.toFloat())
        setBgSelector()
    }

    fun setCornerRadiusTop(cornerRadius: Int) {
        this.cornerRadiusTL = dp2px(cornerRadius.toFloat())
        this.cornerRadiusTR = dp2px(cornerRadius.toFloat())
        setBgSelector()
    }

    fun setCornerRadiusBottom(cornerRadius: Int) {
        this.cornerRadiusBL = dp2px(cornerRadius.toFloat())
        this.cornerRadiusBR = dp2px(cornerRadius.toFloat())
        setBgSelector()
    }

    fun setStrokeWidth(strokeWidth: Int) {
        this.strokeWidth = dp2px(strokeWidth.toFloat())
        setBgSelector()
    }

    fun setDashWidth(dashWidth: Float) {
        this.dashWidth = dashWidth
        setBgSelector()
    }

    fun setDashGap(dashGap: Float) {
        this.dashGap = dashGap
        setBgSelector()
    }

    fun setStrokeColor(strokeColor: Int) {
        this.strokeColor = strokeColor
        setBgSelector()
    }

    fun setStrokePressColor(strokePressColor: Int) {
        this.strokePressColor = strokePressColor
        setBgSelector()
    }

    fun setTextPressColor(textPressColor: Int) {
        this.textPressColor = textPressColor
        setBgSelector()
    }

    fun setRadiusHalfHeight(isRadiusHalfHeight: Boolean) {
        this.isRadiusHalfHeight = isRadiusHalfHeight
        setBgSelector()
    }

    fun setRadiusHalfHeightLeft(isRadiusHalfHeightLeft: Boolean) {
        this.isRadiusHalfHeightLeft = isRadiusHalfHeightLeft
        setBgSelector()
    }

    fun setRadiusHalfHeightRight(isRadiusHalfHeightRight: Boolean) {
        this.isRadiusHalfHeightRight = isRadiusHalfHeightRight
        setBgSelector()
    }

    fun setRadiusHalfWidthTop(isRadiusHalfWidthTop: Boolean) {
        this.isRadiusHalfWidthTop = isRadiusHalfWidthTop
        setBgSelector()
    }

    fun setRadiusHalfWidthBottom(isRadiusHalfWidthBottom: Boolean) {
        this.isRadiusHalfWidthBottom = isRadiusHalfWidthBottom
        setBgSelector()
    }

    fun setWidthHeightEqual(isWidthHeightEqual: Boolean) {
        this.isWidthHeightEqual = isWidthHeightEqual
        setBgSelector()
    }

    fun setCornerRadiusTL(cornerRadius_TL: Int) {
        this.cornerRadiusTL = cornerRadius_TL
        setBgSelector()
    }

    fun setCornerRadiusTR(cornerRadius_TR: Int) {
        this.cornerRadiusTR = cornerRadius_TR
        setBgSelector()
    }

    fun setCornerRadiusBL(cornerRadius_BL: Int) {
        this.cornerRadiusBL = cornerRadius_BL
        setBgSelector()
    }

    fun setCornerRadiusBR(cornerRadius_BR: Int) {
        this.cornerRadiusBR = cornerRadius_BR
        setBgSelector()
    }

    fun getBackgroundColor(): Int {
        return backgroundColor
    }

    fun getBackgroundPressColor(): Int {
        return backgroundPressColor
    }

    fun getCornerRadius(): Int {
        return cornerRadius
    }

    fun getStrokeWidth(): Int {
        return strokeWidth
    }

    fun getDashWidth(): Float {
        return dashWidth
    }

    fun getDashGap(): Float {
        return dashGap
    }

    fun getStrokeColor(): Int {
        return strokeColor
    }

    fun getStrokePressColor(): Int {
        return strokePressColor
    }

    fun getTextPressColor(): Int {
        return textPressColor
    }

    fun isRadiusHalfHeight(): Boolean {
        return isRadiusHalfHeight
    }

    fun isRadiusHalfHeightLeft(): Boolean {
        return isRadiusHalfHeightLeft
    }

    fun isRadiusHalfHeightRight(): Boolean {
        return isRadiusHalfHeightRight
    }

    fun isRadiusHalfWidthTop(): Boolean {
        return isRadiusHalfWidthTop
    }

    fun isRadiusHalfWidthBottom(): Boolean {
        return isRadiusHalfWidthBottom
    }

    fun isWidthHeightEqual(): Boolean {
        return isWidthHeightEqual
    }

    fun getCornerRadiusTL(): Int {
        return cornerRadiusTL
    }

    fun getCornerRadiusTR(): Int {
        return cornerRadiusTR
    }

    fun getCornerRadiusBL(): Int {
        return cornerRadiusBL
    }

    fun getCornerRadiusBR(): Int {
        return cornerRadiusBR
    }

    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun setDrawable(gd: GradientDrawable, color: Int, strokeColor: Int) {
        gd.setColor(color)
        //The corners are ordered top-left, top-right, bottom-right, bottom-left
        if (cornerRadiusTL > 0 || cornerRadiusTR > 0 || cornerRadiusBR > 0 || cornerRadiusBL > 0) {
            radiusArr[0] = cornerRadiusTL.toFloat()
            radiusArr[1] = cornerRadiusTL.toFloat()
            radiusArr[2] = cornerRadiusTR.toFloat()
            radiusArr[3] = cornerRadiusTR.toFloat()
            radiusArr[4] = cornerRadiusBR.toFloat()
            radiusArr[5] = cornerRadiusBR.toFloat()
            radiusArr[6] = cornerRadiusBL.toFloat()
            radiusArr[7] = cornerRadiusBL.toFloat()
            gd.cornerRadii = radiusArr
        } else {
            gd.cornerRadius = cornerRadius.toFloat()
        }
        if (dashWidth > 0 && dashGap > 0)
            gd.setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
        else
            gd.setStroke(strokeWidth, strokeColor)
    }

    fun setBgSelector() {
        val bg = StateListDrawable()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isRippleEnable) {
            setDrawable(gdBackground, backgroundColor, strokeColor)
            val rippleDrawable = RippleDrawable(
                getPressedColorSelector(backgroundColor, backgroundPressColor),
                gdBackground,
                null
            )
            view.background = rippleDrawable
        } else {
            setDrawable(gdBackground, backgroundColor, strokeColor)
            bg.addState(intArrayOf(-android.R.attr.state_pressed), gdBackground)
            if (backgroundPressColor != Integer.MAX_VALUE || strokePressColor != Integer.MAX_VALUE) {
                setDrawable(
                    gdBackgroundPress,
                    if (backgroundPressColor == Integer.MAX_VALUE) backgroundColor else backgroundPressColor,
                    if (strokePressColor == Integer.MAX_VALUE) strokeColor else strokePressColor
                )
                bg.addState(intArrayOf(android.R.attr.state_pressed), gdBackgroundPress)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {//16
                view.background = bg
            } else {
                @Suppress("DEPRECATION")
                view.setBackgroundDrawable(bg)
            }
        }

        if (view is TextView) {
            if (textPressColor != Integer.MAX_VALUE) {
                val textColors = view.textColors
                val colorStateList = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_pressed),
                        intArrayOf(android.R.attr.state_pressed)
                    ),
                    intArrayOf(textColors.defaultColor, textPressColor)
                )
                view.setTextColor(colorStateList)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun getPressedColorSelector(normalColor: Int, pressedColor: Int): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_activated),
                intArrayOf()
            ),
            intArrayOf(pressedColor, pressedColor, pressedColor, normalColor)
        )
    }

    private fun getBackgroundWithGlow(
        view: View,
        backgroundColor: Int,
        glowColor: Int,
        cornerRadius: Int,
        unPressedGlowSize: Int,
        pressedGlowSize: Int
    ): Drawable {
        val outerRadius = FloatArray(8)
        Arrays.fill(outerRadius, cornerRadius.toFloat())
        val shapeDrawablePadding = Rect()
        val shapeDrawable = ShapeDrawable()
        shapeDrawable.setPadding(shapeDrawablePadding)
        shapeDrawable.paint.color = backgroundColor
        shapeDrawable.paint.setShadowLayer(pressedGlowSize.toFloat(), 0f, 0f, glowColor)
        view.setLayerType(LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
        shapeDrawable.shape = RoundRectShape(outerRadius, null, null)
        val drawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable))
        drawable.setLayerInset(
            0,
            unPressedGlowSize,
            unPressedGlowSize,
            unPressedGlowSize,
            unPressedGlowSize
        )
        return drawable
    }
}
