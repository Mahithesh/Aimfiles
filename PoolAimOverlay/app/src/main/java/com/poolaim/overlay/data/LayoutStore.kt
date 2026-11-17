package com.poolaim.overlay.data

import android.content.Context
import com.poolaim.overlay.models.PracticeLayout
import org.json.JSONArray

class LayoutStore(context: Context) {
    private val prefs = context.getSharedPreferences("pool_aim_layouts", Context.MODE_PRIVATE)
    fun all(): List<PracticeLayout> {
        val a = runCatching { JSONArray(prefs.getString("layouts", "[]")) }.getOrElse { JSONArray() }
        return (0 until a.length()).mapNotNull { i -> runCatching { PracticeLayout.fromJson(a.getJSONObject(i)) }.getOrNull() }
    }
    fun save(layout: PracticeLayout) { val list=all().filterNot { it.name==layout.name }.toMutableList(); list.add(layout); persist(list) }
    fun delete(name: String) = persist(all().filterNot { it.name==name })
    fun rename(old: String, new: String) { val l=all().firstOrNull { it.name==old } ?: return; l.name=new; save(l) }
    private fun persist(list: List<PracticeLayout>) { prefs.edit().putString("layouts", JSONArray().apply { list.forEach { put(it.toJson()) } }.toString()).apply() }
}
