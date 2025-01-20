package com.b1097780.glucohub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
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

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Navigation components
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Configure the drawer and bottom navigation
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navViewDrawer: NavigationView = binding.navViewDrawer
        val navViewBottom: BottomNavigationView = binding.navView

        // Define top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
            ),
            drawerLayout
        )

        // Setup ActionBar for navigation
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Connect both navigations to the NavController
        navViewDrawer.setupWithNavController(navController)
        navViewBottom.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
