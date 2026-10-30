package com.udb.mundoclima.model

// ===== REST Countries (modelo interno; se llena desde el JSON v5 con CountryMapper) =====
data class NameDto(val common: String?, val official: String?)
data class FlagsDto(val png: String?, val svg: String?)
data class CurrencyDto(val name: String?, val symbol: String?)

data class CountryDto(
    val name: NameDto?,
    val translations: Map<String, NameDto>? = null,
    val capital: List<String>?,
    val region: String?,
    val subregion: String?,
    val population: Long?,
    val flags: FlagsDto?,
    val cca2: String?,
    val cca3: String?,
    val ccn3: String?,
    val cioc: String?,
    val currencies: Map<String, CurrencyDto>?,
    val languages: Map<String, String>?,
    val latlng: List<Double>?
) {
    fun displayName(): String = translations?.get("spa")?.common ?: name?.common ?: "—"
    fun capitalText(): String = capital?.joinToString(", ").takeUnless { it.isNullOrBlank() } ?: "—"
}

// ===== GeoNames =====
data class GeoStatus(val message: String?, val value: Int?)
data class GeoCity(
    val geonameId: Long,
    val name: String,
    val population: Long?,
    val lat: String,
    val lng: String,
    val fcode: String?
) {
    val latitude: Double get() = lat.toDouble()
    val longitude: Double get() = lng.toDouble()
}
data class GeoNamesResponse(val geonames: List<GeoCity>?, val status: GeoStatus?)

// ===== WeatherAPI =====
data class ConditionDto(val text: String?, val icon: String?)
data class CurrentDto(
    val temp_c: Double?, val temp_f: Double?,
    val condition: ConditionDto?,
    val wind_kph: Double?, val wind_mph: Double?,
    val humidity: Int?,
    val last_updated: String?
)
data class WeatherResponse(val current: CurrentDto?)

// ===== Modelos de la app =====
data class Region(val title: String, val apiName: String) {
    companion object {
        val all = listOf(
            Region("África", "Africa"),
            Region("América", "Americas"),
            Region("Antártida", "Antarctic"),
            Region("Asia", "Asia"),
            Region("Europa", "Europe"),
            Region("Oceanía", "Oceania")
        )
    }
}

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val data: CurrentDto) : WeatherState()
    data class Failed(val message: String) : WeatherState()
}

data class CityItem(
    val city: GeoCity,
    val isCapital: Boolean,
    var weather: WeatherState = WeatherState.Loading
)