package com.example.investmenttracker.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.investmenttracker.R
import com.example.investmenttracker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    var lastMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.mainFragment)
                    item.isEnabled = false
                    lastMenuItem?.isEnabled = true
                    lastMenuItem = item
                    true
                }
                R.id.analytics -> {
                    // TODO create a fragment for analytics
                    item.isEnabled = false
                    lastMenuItem?.isEnabled = true
                    lastMenuItem = item
                    true
                }
                R.id.search -> {
                    navController.navigate(R.id.searchCoinFragment)
                    item.isEnabled = false
                    lastMenuItem?.isEnabled = true
                    lastMenuItem = item
                    true
                }
                else -> false
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        lastMenuItem = null
    }
}