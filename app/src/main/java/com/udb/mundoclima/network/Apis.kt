package com.udb.mundoclima.network

import com.google.gson.JsonElement
import com.udb.mundoclima.model.GeoNamesResponse
import com.udb.mundoclima.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RestCountriesApi {
    @GET("countries/v5")
    suspend fun byRegion(
        @Query("region") region: String,
        @Query("offset") offset: Int = 0
    ): JsonElement

    @GET("countries/v5/codes.alpha_2/{code}")
    suspend fun byCode(@Path("code") code: String): JsonElement
}

interface GeoNamesApi {
    @GET("searchJSON")
    suspend fun cities(
        @Query("country") iso2: String,
        @Query("featureClass") featureClass: String = "P",
        @Query("cities") cities: String = "cities15000",
        @Query("maxRows") maxRows: Int = 10,
        @Query("orderby") orderBy: String = "population",
        @Query("username") username: String
    ): GeoNamesResponse
}

interface WeatherApi {
    @GET("current.json")
    suspend fun current(
        @Query("key") key: String,
        @Query("q") q: String,
        @Query("lang") lang: String = "es"
    ): WeatherResponse
}