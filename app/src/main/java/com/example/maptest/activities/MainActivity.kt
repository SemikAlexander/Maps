package com.example.maptest.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.maptest.databinding.ActivityMainBinding
import com.example.maptest.startActivity

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            supportFragmentManager
                .beginTransaction()
                .replace(mapFrameLayout.id, MapsFragment())
                .commit()

            typeMap.setOnClickListener { startActivity<FreeUseActivity>() }
        }
    }
}