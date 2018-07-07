package com.anwesh.uiprojects.linkedrotthreelineview

/**
 * Created by anweshmishra on 07/07/18.
 */
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.content.Context
import android.graphics.Color
import android.graphics.PointF

val RTL_NODES : Int = 5

class LinkedRotThreeLineView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                prevScale = scale + dir
                dir = 0f
                scale = prevScale
                stopcb(prevScale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RTLNode(var i : Int, val state : State = State()) {

        var next : RTLNode? = null

        var prev : RTLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < RTL_NODES - 1) {
                next = RTLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            paint.strokeWidth = Math.min(w, h) / 60
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = Color.parseColor("#03A9F4")
            val gap : Float = w / RTL_NODES
            val scale : Float = state.scale/3
            val getScale : (Int) -> Float = {i -> Math.min(0.33f, Math.max((i + 1) * scale/3 - 0.33f * i, 0f)) * 3}
            val scales : Array<Float> = arrayOf(getScale(0), getScale(1), getScale(2))
            val pivots : Array<PointF> = arrayOf(PointF(0f, 0f), PointF(gap/2, gap/2), PointF(gap, 0f))
            val rots : Array<Float> = arrayOf(-45f * scales[0], -45f + 90f + scales[1], 45f - 45f + scales[2])
            val size : Float = gap / 2 * Math.sqrt(2.0).toFloat()
            val endYs : Array<Float> = arrayOf(size, -size, size)
            val i : Int = (scale * 3).toInt()
            if (i < scales.size) {
                canvas.save()
                canvas.translate(i * gap + pivots[i].x, h / 2 + pivots[i].y)
                canvas.rotate(rots[i])
                canvas.drawLine(0f, 0f, 0f, endYs[i], paint)
                canvas.restore()
            }
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RTLNode {
            var curr : RTLNode? = prev
            if (dir === 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedRTL(var i : Int) {

        var curr : RTLNode = RTLNode(0)

        var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }
}