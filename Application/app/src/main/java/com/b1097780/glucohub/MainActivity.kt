package com.b1097780.glucohub

import android.os.Bundle
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        applyUserTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup custom toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup NavController
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // AppBarConfiguration for top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
            ),
            binding.drawerLayout
        )

        // Link NavController with toolbar and bottom navigation
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Toolbar custom buttons
        val customMenuButton = findViewById<ImageButton>(R.id.custom_menu_button)
        val customCoinButton = findViewById<ImageButton>(R.id.custom_coin_button)
        val customStreakButton = findViewById<ImageButton>(R.id.custom_streak_button)

        // Drawer button functionality
        customMenuButton.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navViewDrawer)
        }


        // Observe navigation destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_settings, R.id.nav_logout -> {
                    // Hide toolbar buttons and bottom navigation
                    binding.navView.visibility = View.GONE
                    customMenuButton.visibility = View.GONE
                    customCoinButton.visibility = View.GONE
                    customStreakButton.visibility = View.GONE
                }
                else -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    // Show toolbar buttons and bottom navigation
                    binding.navView.visibility = View.VISIBLE
                    customMenuButton.visibility = View.VISIBLE
                    customCoinButton.visibility = View.VISIBLE
                    customStreakButton.visibility = View.VISIBLE
                }
            }
        }

        // Handle navigation drawer actions
        binding.navViewDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    navController.navigate(R.id.navigation_settings)
                }
                R.id.nav_logout -> {
                    performLogout()
                }
                else -> {
                    menuItem.onNavDestinationSelected(navController)
                }
            }
            binding.drawerLayout.closeDrawer(binding.navViewDrawer)
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun applyUserTheme() {
        val sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val themePreference = sharedPreferences.getString("theme", "default")

        when (themePreference) {
            "default" -> setTheme(R.style.Theme_GlucoHub_default)
            "purple" -> setTheme(R.style.Theme_GlucoHub_purple)
            else -> setTheme(R.style.Theme_GlucoHub_default)
        }
    }

    private fun performLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
            .setMessage("This would log you out when implemented.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }
}