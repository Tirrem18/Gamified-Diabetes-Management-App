package com.b1097780.glucohub

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.b1097780.glucohub.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NavController
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
            ),
            binding.drawerLayout
        )

        // Setup ActionBar and Bottom Navigation
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Observe destination changes to toggle Bottom Navigation visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_settings, R.id.nav_logout -> {
                    binding.navView.visibility = View.GONE
                }
                else -> {
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }

        // Handle Drawer Navigation
        binding.navViewDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    navController.navigate(R.id.navigation_settings)
                    binding.drawerLayout.closeDrawer(binding.navViewDrawer)
                    true
                }
                R.id.nav_logout -> {
                    performLogout()
                    binding.drawerLayout.closeDrawer(binding.navViewDrawer)
                    true
                }
                else -> {
                    menuItem.onNavDestinationSelected(navController)
                    binding.drawerLayout.closeDrawer(binding.navViewDrawer)
                    true
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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
