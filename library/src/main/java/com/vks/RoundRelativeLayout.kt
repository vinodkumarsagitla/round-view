package com.vks

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlin.math.max

class RoundRelativeLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :RelativeLayout(context, attrs) {
    /**
     * use delegate to set attr
     */
    val delegate: RoundViewDelegate = RoundViewDelegate(this, context, attrs!!)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (delegate.isWidthHeightEqual() && width > 0 && height > 0) {
            val max = max(width, height)
            val measureSpec = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY)
            super.onMeasure(measureSpec, measureSpec)
            return
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        when {
            delegate.isRadiusHalfHeight() -> delegate.setCornerRadius(height / 2)
            delegate.isRadiusHalfHeightLeft() -> delegate.setCornerRadiusLeft(height / 2)
            delegate.isRadiusHalfHeightRight() -> delegate.setCornerRadiusRight(height / 2)
            delegate.isRadiusHalfWidthTop() -> delegate.setCornerRadiusTop(width / 2)
            delegate.isRadiusHalfWidthBottom() -> delegate.setCornerRadiusBottom(width / 2)
            else -> delegate.setBgSelector()
        }
    }
}
