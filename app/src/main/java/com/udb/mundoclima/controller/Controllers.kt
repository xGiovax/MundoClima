package com.udb.mundoclima.controller

import com.udb.mundoclima.BuildConfig
import com.udb.mundoclima.model.CityItem
import com.udb.mundoclima.model.CountryDto
import com.udb.mundoclima.model.CountryMapper
import com.udb.mundoclima.model.CurrentDto
import com.udb.mundoclima.network.AppException
import com.udb.mundoclima.network.GeoNamesApi
import com.udb.mundoclima.network.RestCountriesApi
import com.udb.mundoclima.network.WeatherApi
import com.udb.mundoclima.network.safeCall

class CountryController(private val api: RestCountriesApi) {

    /** v5 devuelve los países paginados: se piden páginas hasta que meta.more sea false. */
    suspend fun byRegion(region: String): Result<List<CountryDto>> = safeCall {
        val all = mutableListOf<CountryDto>()
        var offset = 0
        var guard = 0
        while (guard++ < 30) {
            val page = CountryMapper.parsePage(api.byRegion(region, offset))
            all += page.items
            if (!page.more || page.items.isEmpty()) break
            offset += page.items.size
        }
        if (all.isEmpty()) throw AppException("No se encontraron países en esta región.")
        all.distinctBy { it.cca2 }.sortedBy { it.displayName() }
    }

    suspend fun byCode(iso2: String): Result<CountryDto> = safeCall {
        CountryMapper.parsePage(api.byCode(iso2)).items.firstOrNull()
            ?: throw AppException("País no encontrado.")
    }
}

class CityController(private val api: GeoNamesApi) {

    suspend fun topCities(iso2: String, capitalNames: List<String>): Result<List<CityItem>> = safeCall {
        val res = api.cities(iso2 = iso2, username = BuildConfig.GEONAMES_USER)

        // GeoNames responde HTTP 200 con un objeto "status" cuando hay error
        res.status?.let {
            throw AppException(
                when (it.value) {
                    10 -> "Usuario de GeoNames inválido."
                    15 -> "No se encontraron ciudades para este país."
                    18, 19, 20 -> "Se alcanzó el límite de solicitudes de GeoNames."
                    else -> it.message ?: "Error de GeoNames."
                }
            )
        }
        val list = res.geonames.orEmpty()
        if (list.isEmpty()) throw AppException("Este país no tiene ciudades disponibles.")

        list.map { c ->
            val cap = c.fcode == "PPLC" || capitalNames.any { it.equals(c.name, ignoreCase = true) }
            CityItem(city = c, isCapital = cap)
        }
    }
}

class WeatherController(private val api: WeatherApi) {

    suspend fun current(lat: Double, lng: Double): Result<CurrentDto> = safeCall {
        api.current(key = BuildConfig.WEATHER_API_KEY, q = "$lat,$lng").current
            ?: throw AppException("Clima no disponible.")
    }
}