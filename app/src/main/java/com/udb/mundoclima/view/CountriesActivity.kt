package com.udb.mundoclima.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.udb.mundoclima.controller.CountryController
import com.udb.mundoclima.databinding.ActivityListBinding
import com.udb.mundoclima.network.ApiClient
import kotlinx.coroutines.launch

class CountriesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_API = "region_api"
        const val EXTRA_TITLE = "region_title"
    }

    private lateinit var b: ActivityListBinding
    private val controller = CountryController(ApiClient.restCountries)
    private lateinit var region: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityListBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.applySystemBarsPadding()

        region = intent.getStringExtra(EXTRA_API).orEmpty()
        b.tvHeader.text = "Países de ${intent.getStringExtra(EXTRA_TITLE).orEmpty()}"

        b.recycler.layoutManager = LinearLayoutManager(this)
        b.btnRetry.setOnClickListener { load() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            b.progress.isVisible = true
            b.errorBox.isVisible = false
            controller.byRegion(region)
                .onSuccess { list ->
                    b.recycler.adapter = CountryAdapter(list) { c ->
                        startActivity(
                            Intent(this@CountriesActivity, CountryDetailActivity::class.java)
                                .putExtra(CountryDetailActivity.EXTRA_ISO2, c.cca2)
                        )
                    }
                }
                .onFailure { showError(it.message ?: "Error") }
            b.progress.isVisible = false
        }
    }

    private fun showError(msg: String) {
        b.tvError.text = msg
        b.errorBox.isVisible = true
    }
}