package com.udb.mundoclima.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.udb.mundoclima.databinding.ItemCityBinding
import com.udb.mundoclima.databinding.ItemCountryBinding
import com.udb.mundoclima.databinding.ItemRegionBinding
import com.udb.mundoclima.model.CityItem
import com.udb.mundoclima.model.CountryDto
import com.udb.mundoclima.model.Region
import com.udb.mundoclima.model.WeatherState
import java.text.NumberFormat

class RegionAdapter(
    private val items: List<Region>,
    private val onClick: (Region) -> Unit
) : RecyclerView.Adapter<RegionAdapter.VH>() {

    class VH(val b: ItemRegionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemRegionBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val r = items[i]
        h.b.tvTitle.text = r.title
        h.b.tvSub.text = "Ver países · ${r.apiName}"
        h.b.root.setOnClickListener { onClick(r) }
    }
}

class CountryAdapter(
    private val items: List<CountryDto>,
    private val onClick: (CountryDto) -> Unit
) : RecyclerView.Adapter<CountryAdapter.VH>() {

    class VH(val b: ItemCountryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemCountryBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val c = items[i]
        h.b.tvName.text = c.displayName()
        h.b.tvCapital.text = "Capital: ${c.capitalText()}"
        h.b.ivFlag.load(c.flags?.png)
        h.b.root.setOnClickListener { onClick(c) }
        h.b.btnOpen.setOnClickListener { onClick(c) }
    }
}

class CityAdapter(
    private val items: List<CityItem>,
    private val onLocate: (CityItem) -> Unit
) : RecyclerView.Adapter<CityAdapter.VH>() {

    class VH(val b: ItemCityBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemCityBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val it = items[i]
        val b = h.b
        val density = b.root.resources.displayMetrics.density

        b.tvCity.text = it.city.name
        b.tvPop.text = "Población: " + NumberFormat.getInstance().format(it.city.population ?: 0)
        b.tvCapitalBadge.isVisible = it.isCapital
        b.card.strokeWidth = if (it.isCapital) (2 * density).toInt() else 0
        b.card.strokeColor = 0xFFF57C00.toInt()
        b.btnLocate.setOnClickListener { _ -> onLocate(it) }

        b.ivWeather.setImageDrawable(null)
        b.tvExtra.text = ""
        b.tvUpdated.text = ""
        b.tvCond.text = ""

        when (val w = it.weather) {
            is WeatherState.Loading -> b.tvTemp.text = "Cargando clima…"
            is WeatherState.Failed -> b.tvTemp.text = "Clima no disponible\n${w.message}"
            is WeatherState.Success -> {
                val d = w.data
                b.tvTemp.text = "${d.temp_c} °C / ${d.temp_f} °F"
                b.tvCond.text = d.condition?.text.orEmpty()
                b.tvExtra.text = "Viento: ${d.wind_kph} km/h (${d.wind_mph} mph) · Humedad: ${d.humidity} %"
                b.tvUpdated.text = "Actualizado: ${d.last_updated}"
                d.condition?.icon?.let { icon -> b.ivWeather.load("https:$icon") }
            }
        }
    }
}
