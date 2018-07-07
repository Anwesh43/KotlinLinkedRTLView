package com.anwesh.uiprojects.linkedrotthreelineview

/**
 * Created by anweshmishra on 07/07/18.
 */
import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.Log

val RTL_NODES : Int = 5

class LinkedRotThreeLineView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    var onCompletionListener : OnCompletionListener? = null

    fun addOnCompletionListener(onComplete : (Int) -> Unit, onReset : (Int) -> Unit) {
        onCompletionListener = OnCompletionListener(onComplete, onReset)
    }

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.05f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
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
            prev?.draw(canvas, paint)
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            paint.strokeWidth = Math.min(w, h) / 60
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = Color.parseColor("#03A9F4")
            val gap : Float = w / RTL_NODES
            val scale : Float = state.scale
            val getScale : (Int) -> Float = {t -> Math.min(0.33f, Math.max((scale) - (0.33f * t), 0f)) * 3}
            val scales : Array<Float> = arrayOf(getScale(0), getScale(1), getScale(2))
            val pivots : Array<PointF> = arrayOf(PointF(0f, 0f), PointF(gap/2, gap/2), PointF(gap, 0f))
            val rots : Array<Float> = arrayOf(-45f * scales[0], -45f + 90f * scales[1], 45f - 45f * scales[2])
            val size : Float = (gap / 2) * Math.sqrt(2.0).toFloat()
            val endYs : Array<Float> = arrayOf(size, -size, size)
            var i : Int = (scale * 3).toInt()
            Log.d("scales at ${i} in ${this.i}", "${scales.joinToString(",")}")
            if ( i == 3) {
                i -= 1
            }
            if (i < 3) {
                canvas.save()
                canvas.translate(this.i * gap + pivots[i].x, h / 2 + pivots[i].y)
                canvas.rotate(rots[i])
                canvas.drawLine(0f, 0f, 0f, endYs[i], paint)
                canvas.restore()
            }
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            state.update {
                stopcb(i, it)
            }
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

        fun update(stopcb : (Int, Float) -> Unit) {
            curr.update {j, scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(j, scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedRotThreeLineView) {

        private val animator : Animator = Animator(view)

        private val linkedRTL : LinkedRTL = LinkedRTL(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            linkedRTL.draw(canvas, paint)
            animator.animate {
                linkedRTL.update {j, scale ->
                    animator.stop()
                    when (scale) {
                        0f -> view.onCompletionListener?.onReset?.invoke(j)
                        1f -> view.onCompletionListener?.onComplete?.invoke(j)
                    }
                }
            }
        }

        fun handleTap() {
            linkedRTL.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity)  : LinkedRotThreeLineView  {
            val view : LinkedRotThreeLineView = LinkedRotThreeLineView(activity)
            activity.setContentView(view)
            return view
        }
    }

    data class OnCompletionListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)
}