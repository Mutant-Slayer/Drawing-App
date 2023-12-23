package com.example.drawingapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath: CustomPath? = null // A variable of CustomPath inner class to use it further
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null // The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
    private var mCanvasPaint: Paint? = null // Instance of canvas paint
    private var mBrushSize: Float = 0.toFloat() // Brush size variable
    private var color = Color.BLACK // Initial color
    private var canvas: Canvas? = null  // The Canvas class holds the draw calls.
                                        // To draw something, you need 4 basic components: A Bitmap to hold the pixels, a Canvas to host the draw calls,
                                        // a Drawing primitive (e.g. Rect, Path, text, Bitmap), and a Paint (to describe the colors and styles for the drawing).
    private var mPaths = ArrayList<CustomPath>() // ArrayList for Paths
    private var mUndoPaths = ArrayList<CustomPath>() // ArrayList for Undo Paths

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.apply {
            this.color = this@DrawingView.color
            this.style = Paint.Style.STROKE // The Style specifies if the primitive being drawn is filled, stroked, or both (in the same color).
            this.strokeJoin = Paint.Join.ROUND // The Join specifies the treatment where lines and curve segments join on a stroked path.
            this.strokeCap = Paint.Cap.ROUND // The Cap specifies the treatment for the beginning and ending of stroked lines and paths.
        }
        mCanvasPaint = Paint(Paint.DITHER_FLAG) // The DITHER_FLAG affects how colors that are higher precision than the device are down-sampled.
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if (mDrawPath!!.isEmpty.not()) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset() // Clear any lines and curves from the path, making it empty
                if (touchX != null && touchY != null) {
                    mDrawPath!!.moveTo(touchX, touchY) // Set the beginning of the next contour to the point (x,y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null && touchY != null) {
                    mDrawPath!!.lineTo(touchX, touchY) // Add a line from the last point to the specified point (x,y)
                }
            }

            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!) // Add when to stroke is drawn to canvas and added in the path array list
                mDrawPath = CustomPath(color, mBrushSize)
            }

            else -> return false
        }

        invalidate()
        return true
    }

    fun onClickUndo() {
        if (mPaths.size > 0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate() // Invalidate the whole view. If the view is visible, onDraw(android.graphics.Canvas) will be called at some point in the future.
        }
    }

    fun onClickRedo() {
        if (mUndoPaths.size > 0) {
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.size - 1))
            invalidate()
        }
    }

    fun setSizeForBrush(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, newSize,
            resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
}