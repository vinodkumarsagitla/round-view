/*
 * Copyright (C) 2015 Vincent Mi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vks

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.Bitmap.Config
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.widget.ImageView.ScaleType
import java.util.*

class RoundedDrawable private constructor(val sourceBitmap: Bitmap) : Drawable() {

    private val mBounds = RectF()
    private val mDrawableRect = RectF()
    private val mBitmapRect = RectF()
    private val mBitmapPaint: Paint
    private val mBitmapWidth: Int
    private val mBitmapHeight: Int
    private val mBorderRect = RectF()
    private val mBorderPaint: Paint
    private val mShaderMatrix = Matrix()
    private val mSquareCornersRect = RectF()

    private var mTileModeX: Shader.TileMode = Shader.TileMode.CLAMP
    private var mTileModeY: Shader.TileMode = Shader.TileMode.CLAMP
    private var mRebuildShader = true

    // [ topLeft, topRight, bottomLeft, bottomRight ]
    private var mCornerRadius = 0f
    private val mCornersRounded = booleanArrayOf(true, true, true, true)

    private var mOval = false
    private var mBorderWidth = 0f
    var borderColors = ColorStateList
        .valueOf(DEFAULT_BORDER_COLOR)
        private set
    private var mScaleType = ScaleType.FIT_CENTER

    val borderColor: Int
        get() = borderColors.defaultColor

    enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    init {
        mBitmapWidth = sourceBitmap.width
        mBitmapHeight = sourceBitmap.height
        mBitmapRect.set(0f, 0f, mBitmapWidth.toFloat(), mBitmapHeight.toFloat())

        mBitmapPaint = Paint()
        mBitmapPaint.style = Paint.Style.FILL
        mBitmapPaint.isAntiAlias = true

        mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        mBorderPaint.strokeWidth = mBorderWidth
    }

    override fun isStateful(): Boolean {
        return borderColors.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = borderColors.getColorForState(state, 0)
        if (mBorderPaint.color != newColor) {
            mBorderPaint.color = newColor
            return true
        } else {
            return super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx: Float
        var dy: Float

        when (mScaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)

                mShaderMatrix.reset()
                mShaderMatrix
                    .setTranslate(
                        ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f).toInt().toFloat(),
                        ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f).toInt().toFloat()
                    )
            }

            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)

                mShaderMatrix.reset()

                dx = 0f
                dy = 0f

                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(
                    (dx + 0.5f).toInt() + mBorderWidth,
                    (dy + 0.5f).toInt() + mBorderWidth
                )
            }

            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.reset()

                if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    scale = 1.0f
                } else {
                    scale = Math.min(
                        mBounds.width() / mBitmapWidth.toFloat(),
                        mBounds.height() / mBitmapHeight.toFloat()
                    )
                }

                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f).toInt().toFloat()
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f).toInt().toFloat()

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)

                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(
                    mBitmapRect, mBorderRect,
                    Matrix.ScaleToFit.FILL
                )
            }
            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(
                    mBitmapRect, mBounds,
                    Matrix.ScaleToFit.CENTER
                )
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(
                    mBitmapRect, mBorderRect,
                    Matrix.ScaleToFit.FILL
                )
            }

            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(
                    mBitmapRect, mBounds,
                    Matrix.ScaleToFit.END
                )
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(
                    mBitmapRect, mBorderRect,
                    Matrix.ScaleToFit.FILL
                )
            }

            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(
                    mBitmapRect, mBounds,
                    Matrix.ScaleToFit.START
                )
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(
                    mBitmapRect, mBorderRect,
                    Matrix.ScaleToFit.FILL
                )
            }

            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setRectToRect(
                    mBitmapRect, mBorderRect,
                    Matrix.ScaleToFit.FILL
                )
            }

            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }

        mDrawableRect.set(mBorderRect)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        mBounds.set(bounds)

        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        if (mRebuildShader) {
            val bitmapShader = BitmapShader(
                sourceBitmap, mTileModeX,
                mTileModeY
            )
            if (mTileModeX == Shader.TileMode.CLAMP && mTileModeY == Shader.TileMode.CLAMP) {
                bitmapShader.setLocalMatrix(mShaderMatrix)
            }
            mBitmapPaint.shader = bitmapShader
            mRebuildShader = false
        }

        if (mOval) {
            if (mBorderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
                canvas.drawOval(mBorderRect, mBorderPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (any(mCornersRounded)) {
                val radius = mCornerRadius
                if (mBorderWidth > 0) {
                    canvas.drawRoundRect(
                        mDrawableRect, radius, radius,
                        mBitmapPaint
                    )
                    canvas.drawRoundRect(
                        mBorderRect, radius, radius,
                        mBorderPaint
                    )
                    redrawBitmapForSquareCorners(canvas)
                    redrawBorderForSquareCorners(canvas)
                } else {
                    canvas.drawRoundRect(
                        mDrawableRect, radius, radius,
                        mBitmapPaint
                    )
                    redrawBitmapForSquareCorners(canvas)
                }
            } else {
                canvas.drawRect(mDrawableRect, mBitmapPaint)
                canvas.drawRect(mBorderRect, mBorderPaint)
            }
        }
    }

    private fun redrawBitmapForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return
        }

        if (mCornerRadius == 0f) {
            return  // no round corners
        }

        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = mCornerRadius

        if (!mCornersRounded[Corner.TOP_LEFT.ordinal]) {
            mSquareCornersRect.set(left, top, left + radius, top + radius)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }

        if (!mCornersRounded[Corner.TOP_RIGHT.ordinal]) {
            mSquareCornersRect.set(right - radius, top, right, radius)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }

        if (!mCornersRounded[Corner.BOTTOM_RIGHT.ordinal]) {
            mSquareCornersRect.set(
                right - radius, bottom - radius, right,
                bottom
            )
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }

        if (!mCornersRounded[Corner.BOTTOM_LEFT.ordinal]) {
            mSquareCornersRect
                .set(left, bottom - radius, left + radius, bottom)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
    }

    private fun redrawBorderForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return
        }

        if (mCornerRadius == 0f) {
            return  // no round corners
        }

        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = mCornerRadius
        val offset = mBorderWidth / 2

        if (!mCornersRounded[Corner.TOP_LEFT.ordinal]) {
            canvas.drawLine(
                left - offset, top, left + radius, top,
                mBorderPaint
            )
            canvas.drawLine(
                left, top - offset, left, top + radius,
                mBorderPaint
            )
        }

        if (!mCornersRounded[Corner.TOP_RIGHT.ordinal]) {
            canvas.drawLine(
                right - radius - offset, top, right, top,
                mBorderPaint
            )
            canvas.drawLine(
                right, top - offset, right, top + radius,
                mBorderPaint
            )
        }

        if (!mCornersRounded[Corner.BOTTOM_RIGHT.ordinal]) {
            canvas.drawLine(
                right - radius - offset, bottom, right + offset,
                bottom, mBorderPaint
            )
            canvas.drawLine(right, bottom - radius, right, bottom, mBorderPaint)
        }

        if (!mCornersRounded[Corner.BOTTOM_LEFT.ordinal]) {
            canvas.drawLine(
                left - offset, bottom, left + radius, bottom,
                mBorderPaint
            )
            canvas.drawLine(left, bottom - radius, left, bottom, mBorderPaint)
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getAlpha(): Int {
        return mBitmapPaint.alpha
    }

    override fun setAlpha(alpha: Int) {
        mBitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return mBitmapPaint.colorFilter
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.colorFilter = cf
        invalidateSelf()
    }

    override fun setDither(dither: Boolean) {
        mBitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    /**
     * @return the corner radius.
     */
    fun getCornerRadius(): Float {
        return mCornerRadius
    }

    /**
     * @param corner the specific corner to get radius of.
     * @return the corner radius of the specified [Corner].
     */
    fun getCornerRadius(corner: Corner): Float {
        return if (mCornersRounded[corner.ordinal]) mCornerRadius else 0f
    }

    /**
     * Sets all corners to the specified radius.
     *
     * @param radius the radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(radius: Float): RoundedDrawable {
        setCornerRadius(radius, radius, radius, radius)
        return this
    }

    /**
     * Sets the corner radius of one specific corner.
     *
     * @param corner the corner.
     * @param radius the radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(corner: Corner, radius: Float): RoundedDrawable {
        if (radius != 0f && mCornerRadius != 0f && mCornerRadius != radius) {
            throw IllegalArgumentException(
                "Multiple nonzero corner radii not yet supported."
            )
        }

        if (radius == 0f) {
            if (only(corner.ordinal, mCornersRounded)) {
                mCornerRadius = 0f
            }
            mCornersRounded[corner.ordinal] = false
        } else {
            if (mCornerRadius == 0f) {
                mCornerRadius = radius
            }
            mCornersRounded[corner.ordinal] = true
        }

        return this
    }

    /**
     * Sets the corner radii of all the corners.
     *
     * @param topLeft     top left corner radius.
     * @param topRight    top right corner radius
     * @param bottomRight bototm right corner radius.
     * @param bottomLeft  bottom left corner radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(
        topLeft: Float, topRight: Float,
        bottomRight: Float, bottomLeft: Float
    ): RoundedDrawable {
        val radiusSet = HashSet<Float>(4)
        radiusSet.add(topLeft)
        radiusSet.add(topRight)
        radiusSet.add(bottomRight)
        radiusSet.add(bottomLeft)

        radiusSet.remove(0f)

        if (radiusSet.size > 1) {
            throw IllegalArgumentException(
                "Multiple nonzero corner radii not yet supported."
            )
        }

        if (!radiusSet.isEmpty()) {
            val radius = radiusSet.iterator().next()
            if (java.lang.Float.isInfinite(radius) || java.lang.Float.isNaN(radius) || radius < 0) {
                throw IllegalArgumentException("Invalid radius value: $radius")
            }
            mCornerRadius = radius
        } else {
            mCornerRadius = 0f
        }

        mCornersRounded[Corner.TOP_LEFT.ordinal] = topLeft > 0
        mCornersRounded[Corner.TOP_RIGHT.ordinal] = topRight > 0
        mCornersRounded[Corner.BOTTOM_RIGHT.ordinal] = bottomRight > 0
        mCornersRounded[Corner.BOTTOM_LEFT.ordinal] = bottomLeft > 0
        return this
    }

    fun getBorderWidth(): Float {
        return mBorderWidth
    }

    fun setBorderWidth(width: Float): RoundedDrawable {
        mBorderWidth = width
        mBorderPaint.strokeWidth = mBorderWidth
        return this
    }

    fun setBorderColor(color: Int): RoundedDrawable {
        return setBorderColor(ColorStateList.valueOf(color))
    }

    fun setBorderColor(colors: ColorStateList?): RoundedDrawable {
        borderColors = colors ?: ColorStateList.valueOf(0)
        mBorderPaint.color = borderColors.getColorForState(
            state,
            DEFAULT_BORDER_COLOR
        )
        return this
    }

    fun isOval(): Boolean {
        return mOval
    }

    fun setOval(oval: Boolean): RoundedDrawable {
        mOval = oval
        return this
    }

    fun getScaleType(): ScaleType {
        return mScaleType
    }

    fun setScaleType(scaleType: ScaleType): RoundedDrawable {
        var mscaleType = scaleType
        if (mscaleType == null) {
            mscaleType = ScaleType.FIT_CENTER
        }
        if (mScaleType != mscaleType) {
            mScaleType = mscaleType
            updateShaderMatrix()
        }
        return this
    }

    fun getTileModeX(): Shader.TileMode {
        return mTileModeX
    }

    fun setTileModeX(tileModeX: Shader.TileMode): RoundedDrawable {
        if (mTileModeX != tileModeX) {
            mTileModeX = tileModeX
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun getTileModeY(): Shader.TileMode {
        return mTileModeY
    }

    fun setTileModeY(tileModeY: Shader.TileMode): RoundedDrawable {
        if (mTileModeY != tileModeY) {
            mTileModeY = tileModeY
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun toBitmap(): Bitmap? {
        return drawableToBitmap(this)
    }

    companion object {

        val DEFAULT_BORDER_COLOR = Color.BLACK

        fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? {
            return if (bitmap != null) {
                RoundedDrawable(bitmap)
            } else {
                null
            }
        }

        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                if (drawable is RoundedDrawable) {
                    // just return if it's already a RoundedDrawable
                    return drawable
                } else if (drawable is LayerDrawable) {
                    val ld = drawable as LayerDrawable?
                    val num = ld!!.numberOfLayers

                    // loop through layers to and change to RoundedDrawables if
                    // possible
                    for (i in 0 until num) {
                        val d = ld.getDrawable(i)
                        ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d))
                    }
                    return ld
                }

                // try to get a bitmap from the drawable and
                val bm = drawableToBitmap(drawable)
                if (bm != null) {
                    return RoundedDrawable(bm)
                }
            }
            return drawable
        }

        private fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            var bitmap: Bitmap?
            val width = Math.max(drawable.intrinsicWidth, 2)
            val height = Math.max(drawable.intrinsicHeight, 2)
            try {
                bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
                bitmap = null
            }

            return bitmap
        }

        private fun only(index: Int, booleans: BooleanArray): Boolean {
            var i = 0
            val len = booleans.size
            while (i < len) {
                if (booleans[i] != (i == index)) {
                    return false
                }
                i++
            }
            return true
        }

        private fun any(booleans: BooleanArray): Boolean {
            for (b in booleans) {
                if (b) {
                    return true
                }
            }
            return false
        }

        private fun all(booleans: BooleanArray): Boolean {
            for (b in booleans) {
                if (b) {
                    return false
                }
            }
            return true
        }
    }
}
