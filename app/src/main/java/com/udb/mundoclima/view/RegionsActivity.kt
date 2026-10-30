package com.udb.mundoclima.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.udb.mundoclima.databinding.ActivityListBinding
import com.udb.mundoclima.model.Region

class RegionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityListBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.applySystemBarsPadding()

        b.tvHeader.text = "Selecciona una región"

        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = RegionAdapter(Region.all) { r ->
            startActivity(
                Intent(this, CountriesActivity::class.java)
                    .putExtra(CountriesActivity.EXTRA_API, r.apiName)
                    .putExtra(CountriesActivity.EXTRA_TITLE, r.title)
            )
        }
    }
}