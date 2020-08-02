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
