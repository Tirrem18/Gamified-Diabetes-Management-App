package com.b1097780.glucohub

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.b1097780.glucohub.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var userCoins: Int = 10

    // CUSTOMISE
    private var theme = "" // Change this to "default", "purple", or "plain"

    override fun onCreate(savedInstanceState: Bundle?) {
        userCoins = PreferencesHelper.getUserCoins(this) // Load coins from PreferencesHelper

        PreferencesHelper.clearAllData(this)
        //PreferencesHelper.populateTestData(this)



        applyUserTheme(theme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavController()
        setupDrawerNavigation()
        setupCustomButtons()
        observeNavDestinationChanges()
        updateCoinButton(userCoins) // Update UI with loaded coins
    }

    private fun applyUserTheme(selectedTheme: String) {
        when (selectedTheme) {
            "default" -> setTheme(R.style.Theme_GlucoHub_default)
            "orange" -> setTheme(R.style.Theme_GlucoHub_orange)
            else -> setTheme(R.style.Theme_GlucoHub_default) // Fallback to default
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupNavController() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.navigation_home,
                    R.id.navigation_glucose,
                    R.id.navigation_planner,
                    R.id.navigation_data
                ),
                binding.drawerLayout
            )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    private fun setupDrawerNavigation() {
        binding.navViewDrawer.setNavigationItemSelectedListener { menuItem ->
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            when (menuItem.itemId) {
                R.id.nav_settings -> navController.navigate(R.id.navigation_settings)
                R.id.nav_profile -> navController.navigate(R.id.navigation_profile)
                R.id.nav_logout -> performLogout()
                else -> menuItem.onNavDestinationSelected(navController)
            }
            binding.drawerLayout.closeDrawer(binding.navViewDrawer)
            true
        }
    }

    fun updateCoinButton(value: Int) {
        val formattedValue = formatNumber(value, false)
        binding.customCoinButton.text = formattedValue
    }

    fun addCoinsFromFragment(amount: Int) {
        addCoins(amount)
    }

    private fun addCoins(amount: Int) {
        userCoins += amount
        updateCoinButton(userCoins)
        PreferencesHelper.setUserCoins(this, userCoins) // Save new coin count
    }

    fun saveLastEntryTime(time: Long) {
        PreferencesHelper.setLastEntryTime(this, time)
    }

    fun loadLastEntryTime(): Long {
        return PreferencesHelper.getLastEntryTime(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun performLogout() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
        builder
            .setTitle("Logout")
            .setMessage("This would log you out when implemented.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun setupCustomButtons() {
        val customMenuButton = findViewById<ImageButton>(R.id.custom_menu_button)
        val customBackButton = findViewById<ImageButton>(R.id.custom_back_button)
        val customStreakButton = findViewById<Button>(R.id.custom_streak_button)
        val customCoinButton = findViewById<Button>(R.id.custom_coin_button)
        val shrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.shrink_button)

        customMenuButton.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper()).postDelayed(
                { binding.drawerLayout.openDrawer(binding.navViewDrawer) }, 150
            )
        }

        customBackButton.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper()).postDelayed(
                { onBackPressedDispatcher.onBackPressed() }, 150
            )
        }

        customStreakButton.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    findNavController(R.id.nav_host_fragment_activity_main)
                        .navigate(R.id.navigation_streaks)
                },
                150
            )
        }

        customCoinButton.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    findNavController(R.id.nav_host_fragment_activity_main)
                        .navigate(R.id.navigation_coins)
                },
                150
            )
        }
    }


    private fun observeNavDestinationChanges() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val customMenuButton = findViewById<ImageButton>(R.id.custom_menu_button)
        val customCoinButton = findViewById<Button>(R.id.custom_coin_button)
        val customStreakButton = findViewById<Button>(R.id.custom_streak_button)
        val customBackButton = findViewById<ImageButton>(R.id.custom_back_button)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_settings,
                R.id.nav_logout,
                R.id.navigation_streaks,
                R.id.navigation_coins,
                R.id.navigation_profile -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.navView.visibility = View.GONE
                    customMenuButton.visibility = View.GONE
                    customCoinButton.visibility = View.GONE
                    customStreakButton.visibility = View.GONE
                    customBackButton.visibility = View.VISIBLE
                }
                else -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.navView.visibility = View.VISIBLE
                    customMenuButton.visibility = View.VISIBLE
                    customCoinButton.visibility = View.VISIBLE
                    customStreakButton.visibility = View.VISIBLE
                    customBackButton.visibility = View.GONE
                }
            }
        }
    }

    private fun formatNumber(value: Int, isStreak: Boolean): String {
        return if (isStreak) {
            when {
                value > 999 -> "1K+"
                else -> value.toString()
            }
        } else {
            when {
                value > 99999 -> "100K+"
                else -> value.toString()
            }
        }
    }


}