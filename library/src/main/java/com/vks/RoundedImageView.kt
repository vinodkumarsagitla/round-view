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


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.ColorFilter
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.vks.RoundedDrawable.Corner
import com.vks.R

class RoundedImageView : AppCompatImageView {
    private val mCornerRadii =
        floatArrayOf(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS)
    private var mBackgroundDrawable: Drawable? = null
    private var borderColors: ColorStateList? =
        ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    private var mColorFilter: ColorFilter? = null
    private var mColorMod = false
    private var mDrawable: Drawable? = null
    private var mHasColorFilter = false
    private var mIsOval = false
    private var mMutateBackground = false
    private var mResource: Int = 0
    private var mScaleType: ScaleType = ScaleType.FIT_CENTER
    private var tileModeX: Shader.TileMode? = DEFAULT_TILE_MODE
        set(tileModeX) {
            if (this.tileModeX == tileModeX) {
                return
            }
            field = tileModeX
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    private var tileModeY: Shader.TileMode? = DEFAULT_TILE_MODE
        set(tileModeY) {
            if (this.tileModeY == tileModeY) {
                return
            }
            field = tileModeY
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    // @Override
    // @Deprecated
    // public void setBackgroundDrawable(Drawable background) {
    // mBackgroundDrawable = background;
    // updateBackgroundDrawableAttrs(true);
    // super.setBackgroundDrawable(mBackgroundDrawable);
    // }

    /**
     * @return the corner radius.
     */
    /**
     * Set the corner radii of all corners in px.
     *
     * @param radius the radius.
     */
    var cornerRadius: Float
        get() {
            for (i in 0..3) {
                if (mCornerRadii[i] > 0) {
                    return mCornerRadii[i]
                }
            }
            return 0f
        }
        set(radius) = setCornerRadius(radius, radius, radius, radius)

    var borderColor: Int
        get() = borderColors!!.defaultColor
        set(color) = setBorderColor(ColorStateList.valueOf(color))

    var isOval: Boolean
        get() = mIsOval
        set(oval) {
            mIsOval = oval
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    constructor(context: Context) : super(context) {}

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet, defStyle: Int = 0) : super(
        context,
        attrs,
        defStyle
    ) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0)

        val index = a.getInt(R.styleable.RoundedImageView_android_scaleType, -1)
        if (index >= 0) {
            setScaleType(SCALE_TYPES[index])
        } else {
            // default scaletype to FIT_CENTER
            setScaleType(ScaleType.FIT_CENTER)
        }
        var cornerRadiusOverride =
            a.getDimensionPixelSize(R.styleable.RoundedImageView_rv_cornerRadius, -1).toFloat()
        mCornerRadii[Corner.TOP_LEFT.ordinal] =
            a.getDimensionPixelSize(R.styleable.RoundedImageView_rv_cornerRadius_TL, -1).toFloat()
        mCornerRadii[Corner.TOP_RIGHT.ordinal] =
            a.getDimensionPixelSize(R.styleable.RoundedImageView_rv_cornerRadius_TR, -1).toFloat()
        mCornerRadii[Corner.BOTTOM_RIGHT.ordinal] =
            a.getDimensionPixelSize(R.styleable.RoundedImageView_rv_cornerRadius_BR, -1).toFloat()
        mCornerRadii[Corner.BOTTOM_LEFT.ordinal] =
            a.getDimensionPixelSize(R.styleable.RoundedImageView_rv_cornerRadius_BL, -1).toFloat()

        var any = false
        run {
            var i = 0
            val len = mCornerRadii.size
            while (i < len) {
                if (mCornerRadii[i] < 0) {
                    mCornerRadii[i] = 0f
                } else {
                    any = true
                }
                i++
            }
        }

        if (!any) {
            if (cornerRadiusOverride < 0) {
                cornerRadiusOverride = DEFAULT_RADIUS
            }
            var i = 0
            val len = mCornerRadii.size
            while (i < len) {
                mCornerRadii[i] = cornerRadiusOverride
                i++
            }
        }

        mBorderWidth =
            a.getDimensionPixelSize(R.styleable.RoundedImageView_rv_strokeWidth, -1).toFloat()
        if (mBorderWidth < 0) {
            mBorderWidth = DEFAULT_BORDER_WIDTH
        }

        borderColors = a.getColorStateList(R.styleable.RoundedImageView_rv_strokeColor)
        if (borderColors == null) {
            borderColors = ColorStateList
                .valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        }

        mMutateBackground = a.getBoolean(R.styleable.RoundedImageView_rv_mutateBackground, false)
        mIsOval = a.getBoolean(R.styleable.RoundedImageView_rv_isOval, false)

        val tileMode = a.getInt(R.styleable.RoundedImageView_rv_tileMode, TILE_MODE_UNDEFINED)
        if (tileMode != TILE_MODE_UNDEFINED) {
            parseTileMode(tileMode)
            parseTileMode(tileMode)
        }

        val tileModeX = a.getInt(R.styleable.RoundedImageView_rv_tileMode_x, TILE_MODE_UNDEFINED)
        if (tileModeX != TILE_MODE_UNDEFINED) {
            parseTileMode(tileModeX)
        }

        val tileModeY = a.getInt(R.styleable.RoundedImageView_rv_tileMode_y, TILE_MODE_UNDEFINED)
        if (tileModeY != TILE_MODE_UNDEFINED) {
            parseTileMode(tileModeY)
        }

        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(true)

        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    override fun getScaleType(): ScaleType {
        return mScaleType
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (mScaleType != scaleType) {
            mScaleType = scaleType
            when (scaleType) {
                ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END, ScaleType.FIT_XY -> super.setScaleType(
                    ScaleType.FIT_XY
                )
                else -> super.setScaleType(scaleType)
            }
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromDrawable(drawable)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageBitmap(bm: Bitmap) {
        mResource = 0
        mDrawable = RoundedDrawable.fromBitmap(bm)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        if (mResource != resId) {
            mResource = resId
            mDrawable = resolveResource()
            updateDrawableAttrs()
            super.setImageDrawable(mDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setImageDrawable(drawable)
    }

    private fun resolveResource(): Drawable? {
        val resources = resources ?: return null
        var d: Drawable? = null
        if (mResource != 0) {
            try {
                @Suppress("DEPRECATION")
                d = resources.getDrawable(mResource)
            } catch (e: Exception) {
                e.printStackTrace()
                // Don't try again.
                mResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }

    // @Override
    // public void setBackground(Drawable background) {
    // // setBackgroundDrawable(background);
    // }

    private fun updateDrawableAttrs() {
        updateAttrs(mDrawable)
    }

    private fun updateBackgroundDrawableAttrs(convert: Boolean) {
        if (mMutateBackground) {
            if (convert) {
                mBackgroundDrawable = RoundedDrawable.fromDrawable(mBackgroundDrawable)
            }
            updateAttrs(mBackgroundDrawable)
        }
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (mColorFilter !== cf) {
            mColorFilter = cf
            mHasColorFilter = true
            mColorMod = true
            applyColorMod()
            invalidate()
        }
    }

    private fun applyColorMod() {
        // Only mutate and apply when modifications have occurred. This should
        // not reset the mColorMod flag, since these filters need to be
        // re-applied if the Drawable is changed.
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable!!.mutate()
            if (mHasColorFilter) {
                mDrawable!!.colorFilter = mColorFilter
            }
            // TODO: support, eventually...
            // mDrawable.setXfermode(mXfermode);
            // mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }

    private fun updateAttrs(drawable: Drawable?) {
        if (drawable == null || tileModeX == null || tileModeY == null) {
            return
        }
        if (drawable is RoundedDrawable) {
            drawable.setScaleType(mScaleType)
                .setBorderWidth(mBorderWidth).setBorderColor(borderColors)
                .setOval(mIsOval).setTileModeX(tileModeX!!)
                .setTileModeY(tileModeY!!)
            drawable.setCornerRadius(
                mCornerRadii[0],
                mCornerRadii[1],
                mCornerRadii[2],
                mCornerRadii[3]
            )
            applyColorMod()
        } else if (drawable is LayerDrawable) {
            // loop through layers to and set drawable attrs
            var i = 0
            val layers = drawable.getNumberOfLayers()
            while (i < layers) {
                updateAttrs(drawable.getDrawable(i))
                i++
            }
        }
    }

    /**
     * Set all the corner radii from a dimension resource id.
     *
     * @param resId dimension resource id of radii.
     */
    fun setCornerRadiusDimen(@DimenRes resId: Int) {
        val radius = resources.getDimension(resId)
        setCornerRadius(radius, radius, radius, radius)
    }

    /**
     * Set the corner radii of each corner individually. Currently only one
     * unique nonzero value is supported.
     *
     * @param topLeft     radius of the top left corner in px.
     * @param topRight    radius of the top right corner in px.
     * @param bottomRight radius of the bottom right corner in px.
     * @param bottomLeft  radius of the bottom left corner in px.
     */
    fun setCornerRadius(
        topLeft: Float, topRight: Float,
        bottomLeft: Float, bottomRight: Float
    ) {
        if (mCornerRadii[Corner.TOP_LEFT.ordinal] == topLeft && mCornerRadii[Corner.TOP_RIGHT.ordinal] == topRight
            && mCornerRadii[Corner.BOTTOM_RIGHT.ordinal] == bottomRight && mCornerRadii[Corner.BOTTOM_LEFT.ordinal] == bottomLeft
        ) return
        mCornerRadii[0] = topLeft
        mCornerRadii[1] = topRight
        mCornerRadii[2] = bottomLeft
        mCornerRadii[3] = bottomRight
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun getBorderWidth(): Float {
        return mBorderWidth
    }

    fun setBorderWidth(@DimenRes resId: Int) {
        setBorderWidth(resources.getDimension(resId))
    }

    fun setBorderWidth(width: Float) {
        if (mBorderWidth == width) return
        mBorderWidth = width
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun setBorderColor(colors: ColorStateList?) {
        if (borderColors == colors) return
        borderColors = colors ?: ColorStateList
            .valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        if (mBorderWidth > 0) {
            invalidate()
        }
    }

    fun mutatesBackground(): Boolean {
        return mMutateBackground
    }

    fun mutateBackground(mutate: Boolean) {
        if (mMutateBackground == mutate) return
        mMutateBackground = mutate
        updateBackgroundDrawableAttrs(true)
        invalidate()
    }

    companion object {
        // Constants for tile mode attributes
        private const val TILE_MODE_UNDEFINED = -2
        private const val TILE_MODE_CLAMP = 0
        private const val TILE_MODE_REPEAT = 1
        private const val TILE_MODE_MIRROR = 2
        const val DEFAULT_RADIUS = 0f
        const val DEFAULT_BORDER_WIDTH = 0f
        val DEFAULT_TILE_MODE = Shader.TileMode.CLAMP
        private val SCALE_TYPES = arrayOf(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )

        private fun parseTileMode(tileMode: Int): Shader.TileMode? {
            return when (tileMode) {
                TILE_MODE_CLAMP -> Shader.TileMode.CLAMP
                TILE_MODE_REPEAT -> Shader.TileMode.REPEAT
                TILE_MODE_MIRROR -> Shader.TileMode.MIRROR
                else -> null
            }
        }
    }
}
