package com.udb.mundoclima.view

import android.os.Bundle
import android.text.Html
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.udb.mundoclima.controller.CityController
import com.udb.mundoclima.controller.CountryController
import com.udb.mundoclima.controller.WeatherController
import com.udb.mundoclima.databinding.ActivityCountryDetailBinding
import com.udb.mundoclima.model.CityItem
import com.udb.mundoclima.model.CountryDto
import com.udb.mundoclima.model.WeatherState
import com.udb.mundoclima.network.ApiClient
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.text.NumberFormat

class CountryDetailActivity : AppCompatActivity() {

    companion object { const val EXTRA_ISO2 = "iso2" }

    private lateinit var b: ActivityCountryDetailBinding
    private val countryCtl = CountryController(ApiClient.restCountries)
    private val cityCtl = CityController(ApiClient.geoNames)
    private val weatherCtl = WeatherController(ApiClient.weather)

    private val items = mutableListOf<CityItem>()
    private val markers = mutableMapOf<Long, Marker>()
    private lateinit var adapter: CityAdapter
    private var iso2 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        b = ActivityCountryDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        iso2 = intent.getStringExtra(EXTRA_ISO2).orEmpty()

        // Mapa
        b.map.setTileSource(TileSourceFactory.MAPNIK)
        b.map.setMultiTouchControls(true)
        b.map.controller.setZoom(4.0)
        // Evita que el scroll de la pantalla "robe" los gestos del mapa
        b.map.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                    v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        // Lista de ciudades
        adapter = CityAdapter(items) { focusCity(it) }
        b.recyclerCities.layoutManager = LinearLayoutManager(this)
        b.recyclerCities.adapter = adapter

        b.btnRefresh.setOnClickListener { if (items.isNotEmpty()) loadWeather() }
        b.btnRetry.setOnClickListener { loadAll() }
        loadAll()
    }

    // ---------- Carga principal ----------
    private fun loadAll() {
        lifecycleScope.launch {
            b.progress.isVisible = true
            b.errorBox.isVisible = false
            b.tvCitiesError.isVisible = false

            val country = countryCtl.byCode(iso2).getOrElse {
                b.progress.isVisible = false
                b.tvError.text = it.message
                b.errorBox.isVisible = true
                b.scroll.isVisible = false
                return@launch
            }
            b.scroll.isVisible = true
            showCountry(country)

            cityCtl.topCities(iso2, country.capital.orEmpty())
                .onSuccess { list ->
                    items.clear(); items.addAll(list)
                    adapter.notifyDataSetChanged()
                    buildMarkers()
                    loadWeather()
                }
                .onFailure {
                    items.clear(); adapter.notifyDataSetChanged()
                    b.tvCitiesError.text = it.message
                    b.tvCitiesError.isVisible = true
                }
            b.progress.isVisible = false
        }
    }

    private fun showCountry(c: CountryDto) {
        title = c.displayName()
        b.ivFlag.load(c.flags?.png)
        b.tvName.text = c.displayName()
        b.tvOfficial.text = "Nombre oficial: ${c.name?.official ?: "—"}"
        val currencies = c.currencies?.values
            ?.joinToString(" · ") { "${it.name} (${it.symbol ?: ""})" } ?: "—"
        val code = c.currencies?.keys?.joinToString(", ") ?: ""
        val html = """
            <b>Capital:</b> ${c.capitalText()}<br>
            <b>Región:</b> ${c.region ?: "—"}<br>
            <b>Subregión:</b> ${c.subregion ?: "—"}<br>
            <b>Población:</b> ${NumberFormat.getInstance().format(c.population ?: 0)}<br>
            <b>Códigos ISO:</b> Alfa-2: ${c.cca2 ?: "—"} · Alfa-3: ${c.cca3 ?: "—"} · Numérico: ${c.ccn3 ?: "—"} · COI: ${c.cioc ?: "—"}<br>
            <b>Monedas:</b> $currencies $code<br>
            <b>Idiomas:</b> ${c.languages?.values?.joinToString(", ") ?: "—"}<br>
            <b>Coordenadas:</b> ${c.latlng?.joinToString(", ") ?: "—"}
        """.trimIndent()
        b.tvInfo.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    }

    // ---------- Clima (cada ciudad es independiente) ----------
    private fun loadWeather() {
        items.forEach { it.weather = WeatherState.Loading }
        adapter.notifyDataSetChanged()
        items.forEach { updateMarker(it) }

        items.forEachIndexed { index, item ->
            lifecycleScope.launch {
                val r = weatherCtl.current(item.city.latitude, item.city.longitude)
                item.weather = r.fold(
                    onSuccess = { WeatherState.Success(it) },
                    onFailure = { WeatherState.Failed(it.message ?: "Error") }
                )
                adapter.notifyItemChanged(index)
                updateMarker(item)
            }
        }
    }

    // ---------- Mapa ----------
    private fun summary(item: CityItem) = when (val w = item.weather) {
        is WeatherState.Loading -> "Cargando clima…"
        is WeatherState.Failed -> "Clima no disponible"
        is WeatherState.Success ->
            "${w.data.temp_c} °C / ${w.data.temp_f} °F · ${w.data.condition?.text.orEmpty()}"
    }

    private fun buildMarkers() {
        b.map.overlays.clear(); markers.clear()
        items.forEach { item ->
            val m = Marker(b.map).apply {
                position = GeoPoint(item.city.latitude, item.city.longitude)
                title = item.city.name + if (item.isCapital) " ★" else ""
                snippet = summary(item)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            markers[item.city.geonameId] = m
            b.map.overlays.add(m)
        }
        val pts = items.map { GeoPoint(it.city.latitude, it.city.longitude) }
        b.map.post {
            if (pts.size == 1) {
                b.map.controller.setCenter(pts[0])
            } else if (pts.isNotEmpty()) {
                b.map.zoomToBoundingBox(BoundingBox.fromGeoPoints(pts), true, 80)
            }
        }
        b.map.invalidate()
    }

    private fun updateMarker(item: CityItem) {
        markers[item.city.geonameId]?.snippet = summary(item)
        b.map.invalidate()
    }

    private fun focusCity(item: CityItem) {
        val m = markers[item.city.geonameId] ?: return
        b.scroll.smoothScrollTo(0, b.map.top)
        b.map.controller.setZoom(9.0)
        b.map.controller.animateTo(m.position)
        m.showInfoWindow()
    }

    override fun onResume() { super.onResume(); b.map.onResume() }
    override fun onPause() { super.onPause(); b.map.onPause() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
