package com.poolaim.overlay.drawing

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.poolaim.overlay.models.PracticeLayout
import com.poolaim.overlay.models.PracticePoint
import kotlin.math.abs
import kotlin.math.hypot

class PracticeCanvasView(context: Context, val layout: PracticeLayout, private val changed: () -> Unit, private val moved: (Int,Int,Int,Int) -> Unit) : View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private var downX=0f; private var downY=0f; private var mode=0; private var selected: PracticePoint?=null
    private val hit=28f
    init { setLayerType(View.LAYER_TYPE_HARDWARE,null); isClickable=true }
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if(layout.alpha>0) { paint.color=Color.argb((layout.alpha*255/100),Color.red(layout.fillColor),Color.green(layout.fillColor),Color.blue(layout.fillColor)); paint.style=Paint.Style.FILL; c.drawRect(0f,0f,width.toFloat(),height.toFloat(),paint) }
        paint.color=layout.borderColor; paint.style=Paint.Style.STROKE; paint.strokeWidth=layout.borderWidth; c.drawRect(layout.borderWidth/2,layout.borderWidth/2,width-layout.borderWidth/2,height-layout.borderWidth/2,paint)
        layout.points.forEach { p ->
            paint.color=p.color; paint.strokeWidth=layout.lineWidth; paint.style=Paint.Style.STROKE
            c.drawLine(0f,p.y,width.toFloat(),p.y,paint); c.drawLine(p.x,0f,p.x,height.toFloat(),paint)
            paint.style=Paint.Style.FILL; paint.color=Color.argb(45,Color.red(p.color),Color.green(p.color),Color.blue(p.color)); c.drawCircle(p.x,p.y,layout.diameter/2,paint)
            paint.style=Paint.Style.STROKE; paint.strokeWidth=2.5f; paint.color=p.color; c.drawCircle(p.x,p.y,layout.diameter/2,paint)
            if(p.label.isNotBlank()) { paint.style=Paint.Style.FILL; paint.textSize=18f; paint.color=Color.WHITE; c.drawText(p.label,p.x+layout.diameter/2+5,p.y-5,paint) }
        }
        // Resize affordances remain visible independently of the fill alpha.
        paint.color=Color.WHITE; paint.style=Paint.Style.FILL
        listOf(0f to 0f, width.toFloat() to 0f, 0f to height.toFloat(), width.toFloat() to height.toFloat()).forEach { c.drawCircle(it.first,it.second,7f,paint) }
    }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x=e.x; val y=e.y
        when(e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX=x; downY=y; selected=layout.points.minByOrNull { hypot((it.x-x).toDouble(),(it.y-y).toDouble()) }?.takeIf { hypot((it.x-x).toDouble(),(it.y-y).toDouble()) < maxOf(hit,layout.diameter) }
                mode=when { layout.panMode -> 1; selected!=null -> 2; x<24&&y<24 || x>width-24&&y<24 || x<24&&y>height-24 || x>width-24&&y>height-24 -> 3; else -> 4 }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx=x-downX; val dy=y-downY
                when(mode) { 1 -> { moved(dx.toInt(),dy.toInt(),0,0); downX=x; downY=y }; 2 -> selected?.let { it.x=(it.x+dx).coerceIn(0f,width.toFloat()); it.y=(it.y+dy).coerceIn(0f,height.toFloat()); downX=x; downY=y; invalidate() }; 3 -> { moved(0,0,dx.toInt(),dy.toInt()); downX=x; downY=y } }
                return true
            }
            MotionEvent.ACTION_UP -> { if(mode==4 && x in 0f..width.toFloat() && y in 0f..height.toFloat()) layout.points.add(PracticePoint(x=x,y=y)); changed(); invalidate(); mode=0; return true }
            MotionEvent.ACTION_CANCEL -> { mode=0; return true }
        }
        return true
    }
    fun undo(){ if(layout.points.isNotEmpty()) layout.points.removeAt(layout.points.lastIndex); invalidate(); changed() }
    fun deleteSelected(){ selected?.let { layout.points.remove(it); selected=null; invalidate(); changed() } }
    fun clear(){ layout.points.clear(); selected=null; invalidate(); changed() }
    fun setPan(v:Boolean){ layout.panMode=v; invalidate() }
    fun setDiameter(v:Float){ layout.diameter=v; invalidate() }
    fun setLineWidth(v:Float){ layout.lineWidth=v; invalidate() }
    fun setBorderWidth(v:Float){ layout.borderWidth=v; invalidate() }
    fun setAlpha(v:Int){ layout.alpha=v; invalidate() }
    fun setLineColor(v:Int){ layout.lineColor=v; layout.points.forEach { it.color=v }; invalidate() }
}
