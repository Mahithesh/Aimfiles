package com.poolaim.overlay.models

import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PracticePoint(
    val id: String = UUID.randomUUID().toString(),
    var x: Float,
    var y: Float,
    var color: Int = Color.CYAN,
    var label: String = ""
)

data class PracticeLayout(
    var name: String,
    var rectX: Int = 70,
    var rectY: Int = 180,
    var rectW: Int = 760,
    var rectH: Int = 480,
    var alpha: Int = 0,
    var diameter: Float = 28f,
    var lineWidth: Float = 2f,
    var borderWidth: Float = 3f,
    var borderColor: Int = Color.CYAN,
    var fillColor: Int = Color.TRANSPARENT,
    var lineColor: Int = Color.CYAN,
    var panMode: Boolean = false,
    val points: MutableList<PracticePoint> = mutableListOf()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name); put("rectX", rectX); put("rectY", rectY); put("rectW", rectW); put("rectH", rectH)
        put("alpha", alpha); put("diameter", diameter.toDouble()); put("lineWidth", lineWidth.toDouble())
        put("borderWidth", borderWidth.toDouble()); put("borderColor", borderColor); put("fillColor", fillColor)
        put("lineColor", lineColor); put("panMode", panMode)
        put("points", JSONArray().apply { points.forEach { p -> put(JSONObject().put("id",p.id).put("x",p.x.toDouble()).put("y",p.y.toDouble()).put("color",p.color).put("label",p.label)) } })
    }
    companion object {
        fun fromJson(o: JSONObject): PracticeLayout {
            val l = PracticeLayout(o.optString("name", "Practice"))
            l.rectX=o.optInt("rectX",70); l.rectY=o.optInt("rectY",180); l.rectW=o.optInt("rectW",760); l.rectH=o.optInt("rectH",480)
            l.alpha=o.optInt("alpha",0); l.diameter=o.optDouble("diameter",28.0).toFloat(); l.lineWidth=o.optDouble("lineWidth",2.0).toFloat()
            l.borderWidth=o.optDouble("borderWidth",3.0).toFloat(); l.borderColor=o.optInt("borderColor",Color.CYAN); l.fillColor=o.optInt("fillColor",Color.TRANSPARENT)
            l.lineColor=o.optInt("lineColor",Color.CYAN); l.panMode=o.optBoolean("panMode",false)
            val a=o.optJSONArray("points") ?: JSONArray(); for(i in 0 until a.length()) { val p=a.getJSONObject(i); l.points.add(PracticePoint(p.optString("id"),p.optDouble("x").toFloat(),p.optDouble("y").toFloat(),p.optInt("color",Color.CYAN),p.optString("label"))) }
            return l
        }
    }
}
