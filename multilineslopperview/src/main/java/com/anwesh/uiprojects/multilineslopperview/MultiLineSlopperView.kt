package com.anwesh.uiprojects.multilineslopperview

/**
 * Created by anweshmishra on 03/08/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val colors : Array<String> = arrayOf("#3F51B5", "#009688", "#03A9F4", "#F44336", "")
val parts : Int = 3
val scGap : Float = 0.2f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 2f
val lines : Int = 4
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawMultiLineSlopper(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts + 1)
    val sf2 : Float = sf.divideScale(1, parts + 1)
    val sf3 : Float = sf.divideScale(2, parts + 1)
    val wSize : Float = w / sizeFactor
    val hSize : Float = h / sizeFactor
    val xGap : Float = (w / sizeFactor) / lines
    val yGap : Float = (h / sizeFactor) / (lines)
    val yInit : Float = h / 2 + hSize / 2
    val xInit : Float = w / 2 - wSize / 2
    val yStart : Float = yInit - lines * yGap
    val yEnd : Float = yInit - yGap
    val y : Float = yStart + (yEnd - yStart) * sf3
    for (j in 0..(lines - 1)) {
        save()
        translate(xInit + xGap * j, yInit)
        drawLine(0f, 0f, 0f, yGap * (lines - j) * sf1, paint)
        restore()
    }
    drawLine(xInit, yInit, xInit + wSize * sf2, yInit, paint)
    drawLine(xInit, yStart, xInit + wSize * sf3, y, paint)
}

fun Canvas.drawMLSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = Color.parseColor(colors[i])
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    drawMultiLineSlopper(scale, w, h, paint)
    restore()
}

class MultiLineSlopperView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
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

    data class MLSNode(var i : Int, val state : State = State()) {

        private var next : MLSNode? = null
        private var prev : MLSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = MLSNode(i)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMLSNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : MLSNode {
            var curr : MLSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class MultiLineSlopper(var i : Int) {

        private var curr : MLSNode = MLSNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : MultiLineSlopperView) {

        private val animator : Animator = Animator(view)
        private val mls : MultiLineSlopper = MultiLineSlopper(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            mls.draw(canvas, paint)
            animator.animate {
                mls.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            mls.startUpdating {
                animator.start()
            }
        }
    }
}