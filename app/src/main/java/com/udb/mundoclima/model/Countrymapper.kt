package com.udb.mundoclima.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/** Convierte la respuesta de REST Countries v5 en nuestro modelo CountryDto. */
object CountryMapper {

    data class Page(val items: List<CountryDto>, val more: Boolean)

    /** Acepta {data:{objects:[...],meta:{more}}}, un arreglo, o un país suelto. */
    fun parsePage(root: JsonElement?): Page {
        if (root == null || root.isJsonNull) return Page(emptyList(), false)
        val objs = mutableListOf<JsonObject>()
        var more = false
        when {
            root.isJsonArray -> root.asJsonArray.forEach { if (it.isJsonObject) objs += it.asJsonObject }
            root.isJsonObject -> {
                val r = root.asJsonObject
                val data = r.obj("data")
                if (data != null) {
                    data.arr("objects")?.forEach { if (it.isJsonObject) objs += it.asJsonObject }
                    more = data.obj("meta")?.get("more")?.let { it.isJsonPrimitive && it.asBoolean } ?: false
                } else if (r.has("names") || r.has("codes")) {
                    objs += r
                }
            }
        }
        return Page(objs.map { fromJson(it) }, more)
    }

    fun fromJson(o: JsonObject): CountryDto {
        val names = o.obj("names")
        val codes = o.obj("codes")
        val flag = o.obj("flag")

        val capitals = o.arr("capitals")
        val capitalNames = capitals?.mapNotNull { if (it.isJsonObject) it.asJsonObject.str("name") else null }

        val currencies = o.arr("currencies")?.mapNotNull { e ->
            if (!e.isJsonObject) null else e.asJsonObject.let { c ->
                (c.str("code") ?: c.str("name") ?: "?") to CurrencyDto(c.str("name"), c.str("symbol"))
            }
        }?.toMap()

        val languages = o.arr("languages")?.mapNotNull { e ->
            if (!e.isJsonObject) null else e.asJsonObject.let { l ->
                val name = l.str("name")
                if (name == null) null else (l.str("bcp47") ?: name) to name
            }
        }?.toMap()

        return CountryDto(
            name = NameDto(names?.str("common"), names?.str("official")),
            translations = null,
            capital = capitalNames,
            region = o.str("region"),
            subregion = o.str("subregion"),
            population = findNumber(o, "population")?.toLong(),
            flags = FlagsDto(flag?.str("url_png"), flag?.str("url_svg")),
            cca2 = codes?.str("alpha_2"),
            cca3 = codes?.str("alpha_3"),
            ccn3 = codes?.str("ccn3"),
            cioc = codes?.str("cioc"),
            currencies = currencies,
            languages = languages,
            latlng = findLatLng(o)
        )
    }

    // ---------- helpers ----------
    private fun JsonObject.obj(k: String): JsonObject? = get(k)?.takeIf { it.isJsonObject }?.asJsonObject
    private fun JsonObject.arr(k: String): JsonArray? = get(k)?.takeIf { it.isJsonArray }?.asJsonArray
    private fun JsonObject.str(k: String): String? =
        get(k)?.takeIf { it.isJsonPrimitive }?.asString
    private fun JsonObject.num(k: String): Double? =
        get(k)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asDouble

    /** Busca recursivamente el primer número con esa clave (ej. "population"). */
    private fun findNumber(e: JsonElement?, key: String): Double? {
        if (e == null) return null
        if (e.isJsonObject) {
            val o = e.asJsonObject
            o.num(key)?.let { return it }
            for ((_, v) in o.entrySet()) findNumber(v, key)?.let { return it }
        } else if (e.isJsonArray) {
            for (x in e.asJsonArray) findNumber(x, key)?.let { return it }
        }
        return null
    }

    private fun latLngOf(c: JsonObject?): List<Double>? {
        if (c == null) return null
        val lat = c.num("lat") ?: c.num("latitude")
        val lng = c.num("lng") ?: c.num("lon") ?: c.num("longitude")
        return if (lat != null && lng != null) listOf(lat, lng) else null
    }

    private fun findLatLng(o: JsonObject): List<Double>? {
        latLngOf(o.obj("coordinates"))?.let { return it }
        latLngOf(o.obj("geography")?.obj("coordinates"))?.let { return it }
        latLngOf(o.obj("location")?.obj("coordinates"))?.let { return it }
        // Respaldo: coordenadas de la capital principal
        val caps = o.arr("capitals") ?: return null
        for (c in caps) if (c.isJsonObject) latLngOf(c.asJsonObject.obj("coordinates"))?.let { return it }
        return null
    }
}