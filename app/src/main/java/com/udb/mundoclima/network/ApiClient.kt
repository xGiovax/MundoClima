package com.udb.mundoclima.network

import com.udb.mundoclima.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private fun client(bearer: String? = null): OkHttpClient {
        val b = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
        if (!bearer.isNullOrBlank()) {
            b.addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $bearer")
                        .build()
                )
            }
        }
        return b.build()
    }

    private fun retrofit(baseUrl: String, http: OkHttpClient) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val restCountries: RestCountriesApi by lazy {
        retrofit("https://api.restcountries.com/", client(BuildConfig.RESTCOUNTRIES_TOKEN))
            .create(RestCountriesApi::class.java)
    }
    val geoNames: GeoNamesApi by lazy {
        retrofit("https://secure.geonames.org/", client()).create(GeoNamesApi::class.java)
    }
    val weather: WeatherApi by lazy {
        retrofit("https://api.weatherapi.com/v1/", client()).create(WeatherApi::class.java)
    }
}
