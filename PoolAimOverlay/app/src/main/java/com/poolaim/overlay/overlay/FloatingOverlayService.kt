package com.poolaim.overlay.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.*
import com.poolaim.overlay.MainActivity
import com.poolaim.overlay.data.LayoutStore
import com.poolaim.overlay.drawing.PracticeCanvasView
import com.poolaim.overlay.models.PracticeLayout

class FloatingOverlayService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var model: PracticeLayout
    private var canvas: PracticeCanvasView?=null
    private var drawingParams: WindowManager.LayoutParams?=null
    private var panel: View?=null
    private var panelParams: WindowManager.LayoutParams?=null
    private var hidden=false
    private val store by lazy { LayoutStore(this) }

    override fun onCreate() { super.onCreate(); wm=getSystemService(WINDOW_SERVICE) as WindowManager; model=PracticeLayout("Current Practice"); startForegroundCompat(); showOverlay() }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) { ACTION_STOP -> stopSelf(); ACTION_HIDE -> { hidden=!hidden; canvas?.visibility=if(hidden) View.GONE else View.VISIBLE }; ACTION_SHOW -> { hidden=false; canvas?.visibility=View.VISIBLE } }
        return START_STICKY
    }
    private fun startForegroundCompat() {
        val channel="pool_aim_overlay"
        if(Build.VERSION.SDK_INT>=26) { val nm=getSystemService(NotificationManager::class.java); nm.createNotificationChannel(NotificationChannel(channel,"Pool Aim Overlay",NotificationManager.IMPORTANCE_LOW)) }
        val pending=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n=if(Build.VERSION.SDK_INT>=26) Notification.Builder(this,channel).setContentTitle("Pool Aim Overlay active").setContentText("Floating practice tools are available").setSmallIcon(android.R.drawable.ic_menu_compass).setContentIntent(pending).build() else @Suppress("DEPRECATION") Notification.Builder(this).setContentTitle("Pool Aim Overlay").setSmallIcon(android.R.drawable.ic_menu_compass).build()
        if(Build.VERSION.SDK_INT>=34) startForeground(41,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(41,n)
    }
    private fun lp(x:Int,y:Int,w:Int,h:Int, flags:Int=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE): WindowManager.LayoutParams {
        val type=if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        return WindowManager.LayoutParams(w,h,type,flags,PixelFormat.TRANSLUCENT).apply { gravity=Gravity.TOP or Gravity.LEFT; this.x=x; this.y=y }
    }
    private fun showOverlay() {
        if(!Settings.canDrawOverlays(this)) { stopSelf(); return }
        val view=PracticeCanvasView(this,model,{ store.save(model) },{ dx,dy,rw,rh ->
            drawingParams?.let { p -> if(dx!=0||dy!=0){p.x+=dx;p.y+=dy;model.rectX=p.x;model.rectY=p.y;wm.updateViewLayout(canvas,p)}; if(rw!=0||rh!=0){p.width=(p.width+rw).coerceIn(180,1600);p.height=(p.height+rh).coerceIn(140,1200);model.rectW=p.width;model.rectH=p.height; model.points.forEach { it.x=it.x.coerceIn(0f,p.width.toFloat()); it.y=it.y.coerceIn(0f,p.height.toFloat()) }; canvas?.layoutParams=p; wm.updateViewLayout(canvas,p)} }
        })
        canvas=view; val dp=lp(model.rectX,model.rectY,model.rectW,model.rectH,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL); drawingParams=dp
        runCatching { wm.addView(view,dp) }.onFailure { stopSelf(); return }
        buildPanel()
    }
    private fun buildPanel() {
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(12,10,12,10); background=GradientDrawable().apply { setColor(Color.argb(235,18,27,38)); cornerRadius=24f; setStroke(2,Color.rgb(67,214,164)) } }
        val title=TextView(this).apply { text="POOL AIM  ·  PRACTICE"; setTextColor(Color.WHITE); textSize=13f; setPadding(4,4,4,10) }; root.addView(title)
        fun button(label:String, action:()->Unit) { root.addView(Button(this).apply { text=label; isAllCaps=false; setOnClickListener { action() } },LinearLayout.LayoutParams(-1,44).apply { bottomMargin=4 }) }
        button("Pan Mode: ${if(model.panMode) "ON" else "OFF"}") { model.panMode=!model.panMode; canvas?.setPan(model.panMode); store.save(model); rebuildPanel() }
        button("Undo") { canvas?.undo() }; button("Delete selected") { canvas?.deleteSelected() }; button("Clear points") { canvas?.clear() }
        button("Save layout") { store.save(model); Toast.makeText(this,"Layout saved",Toast.LENGTH_SHORT).show() }
        button("Load saved layout") { val saved=store.all().firstOrNull(); if(saved!=null){ model=saved; removeWindows(); showOverlay() } else Toast.makeText(this,"No saved layouts",Toast.LENGTH_SHORT).show() }
        button("Show / hide drawing") { hidden=!hidden; canvas?.visibility=if(hidden) View.GONE else View.VISIBLE }
        button("Close floating mode") { stopSelf() }
        addSlider(root,"Background opacity",0,100,model.alpha) { model.alpha=it; canvas?.setAlpha(it); store.save(model) }
        addSlider(root,"Circle diameter",12,80,model.diameter.toInt()) { canvas?.setDiameter(it.toFloat()); store.save(model) }
        addSlider(root,"Guide thickness",1,12,model.lineWidth.toInt().coerceAtLeast(1)) { canvas?.setLineWidth(it.toFloat()); store.save(model) }
        addSlider(root,"Border thickness",1,12,model.borderWidth.toInt().coerceAtLeast(1)) { canvas?.setBorderWidth(it.toFloat()); store.save(model) }
        val scroll=ScrollView(this).apply { addView(root) }
        val pp=lp(18,90,220,WindowManager.LayoutParams.WRAP_CONTENT); pp.flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL; panelParams=pp; panel=scroll
        // Drag only from title bar; all controls remain independently clickable.
        title.setOnTouchListener(object:View.OnTouchListener { var sx=0f; var sy=0f; var ox=0; var oy=0; override fun onTouch(v:View,e:MotionEvent):Boolean { when(e.action){MotionEvent.ACTION_DOWN->{sx=e.rawX;sy=e.rawY;ox=pp.x;oy=pp.y;return true};MotionEvent.ACTION_MOVE->{pp.x=ox+(e.rawX-sx).toInt();pp.y=oy+(e.rawY-sy).toInt();runCatching{wm.updateViewLayout(scroll,pp)};return true}};return true } })
        runCatching { wm.addView(scroll,pp) }
    }
    private fun addSlider(root:LinearLayout,label:String,min:Int,max:Int,value:Int,onChange:(Int)->Unit) { root.addView(TextView(this).apply { text=label; textSize=11f; setTextColor(Color.LTGRAY) }); root.addView(SeekBar(this).apply { this.max=max-min; progress=(value-min).coerceIn(0,this.max); setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){if(u)onChange(p+min)};override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){} }) }) }
    private fun rebuildPanel(){ removePanel(); buildPanel() }
    private fun removePanel(){ panel?.let { runCatching { wm.removeView(it) } }; panel=null }
    private fun removeWindows(){ removePanel(); canvas?.let { runCatching { wm.removeView(it) } }; canvas=null }
    override fun onDestroy(){ removeWindows(); super.onDestroy() }
    override fun onBind(intent:Intent?):IBinder?=null
    companion object { const val ACTION_STOP="poolaim.STOP"; const val ACTION_HIDE="poolaim.HIDE"; const val ACTION_SHOW="poolaim.SHOW" }
}
