package com.b1097780.glucohub

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.google.firebase.FirebaseApp


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var userCoins: Int = 10
    private var userStreak: Int = 0 // Variable to store the streak

    // CUSTOMISE
    private var theme = "" // Change this to "default", "purple", or "plain"

    override fun onCreate(savedInstanceState: Bundle?) {


        //PreferencesHelper.clearAllData(this) // Reset all stored values
        //PreferencesHelper.populateTestData(this) // Fill with test data

        //PreferencesHelper.setLastStreakDate(this, "20250309") // Set last streak to 3 days ago (break streak)

        PreferencesHelper.setUserStreak(this, 1149) // Manually set current streak
        //PreferencesHelper.setHighestStreak(this, 15) // Manually set highest streak

        //PreferencesHelper.setMilestoneClaimed(this, 7) // Mark 7-day milestone as claimed
        //PreferencesHelper.setMilestoneClaimed(this, 30) // Mark 30-day milestone as claimed

        //PreferencesHelper.setCoinMultiplier(this, 2) // Manually set multiplier to x2
        //PreferencesHelper.addCoins(this, 100) // Add 100 coins

        //PreferencesHelper.setUserTheme(this, "") // Add 100 coins




        PreferencesHelper.checkAndResetStreak(this)
        userCoins = PreferencesHelper.getUserCoins(this) // Load coins from PreferencesHelper
        userStreak = PreferencesHelper.getUserStreak(this) // Load streak
        theme = PreferencesHelper.getUserTheme(this) // Load theme

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
        updateStreakButton(userStreak)




        try {
            // ✅ Make sure Firebase is initialized BEFORE using FirebaseHelper
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("MainActivity", "✅ FirebaseApp manually initialized in MainActivity")
            }

            // Now we initialize FirebaseHelper
            firebaseHelper = FirebaseHelper(this)
            Log.d("MainActivity", "✅ FirebaseHelper Initialized in MainActivity")

        } catch (e: Exception) {
            Log.e("MainActivity", "❌ FirebaseHelper Initialization Failed: ${e.message}")
        }

        firebaseHelper.signInAnonymously()

    }

    fun applyUserTheme(selectedTheme: String) {
        when (selectedTheme) {
            "default" -> setTheme(R.style.Theme_GlucoHub_default)
            "spooky" -> setTheme(R.style.Theme_GlucoHub_orange)
            "bubblegum" -> setTheme(R.style.Theme_GlucoHub_bubblegum)
            "dragonfruit" -> setTheme(R.style.Theme_GlucoHub_dragonfruit)
            "peppermint" -> setTheme(R.style.Theme_GlucoHub_peppermint)
            else -> setTheme(R.style.Theme_GlucoHub_default)
        }

    }

    fun reloadTheme() {
        recreate() // Reloads the activity to apply theme changes
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
                    R.id.navigation_logs,
                    R.id.navigation_profile
                ),
                binding.drawerLayout
            )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    private fun setupDrawerNavigation() {
        binding.navViewDrawer.setNavigationItemSelectedListener { menuItem ->
            val navController = findNavController(R.id.nav_host_fragment_activity_main)

            // Prevent duplicate fragment creation
            if (navController.currentDestination?.id == menuItem.itemId) {
                binding.drawerLayout.closeDrawer(binding.navViewDrawer)
                return@setNavigationItemSelectedListener true
            }

            // Clear back stack before navigating
            navController.popBackStack(R.id.navigation_home, false) // Keep Home in stack

            when (menuItem.itemId) {
                R.id.nav_settings -> navController.navigate(R.id.navigation_settings)
                R.id.nav_find_users -> navController.navigate(R.id.navigation_find_users)
                R.id.nav_logout -> performLogout()
                else -> menuItem.onNavDestinationSelected(navController)
            }

            // Close the drawer immediately
            binding.drawerLayout.closeDrawer(binding.navViewDrawer)

            true
        }
    }


    fun updateCoinButton(value: Int) {
        val formattedValue = formatNumber(value, false)
        binding.customCoinButton.text = formattedValue
    }

    // Function to update streak button text
    fun updateStreakButton(value: Int) {
        val formattedValue = formatNumber(value, true)
        binding.customStreakButton.text = formattedValue
    }


    fun addCoinsFromFragment(amount: Int) {
        addCoins(amount)
    }


    private fun addCoins(amount: Int) {
        userCoins += amount
        updateCoinButton(userCoins)
        PreferencesHelper.setUserCoins(this, userCoins) // Save new coin count
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
            disableButtonTemporarily(it)

            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper()).postDelayed(
                { binding.drawerLayout.openDrawer(binding.navViewDrawer) }, 150
            )
        }

        customBackButton.setOnClickListener {
            disableButtonTemporarily(it)

            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper()).postDelayed(
                { onBackPressedDispatcher.onBackPressed() }, 150
            )
        }

        customStreakButton.setOnClickListener {
            disableButtonTemporarily(it)

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
            disableButtonTemporarily(it)

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

    // ✅ Function to disable button for 0.5 seconds
    private fun disableButtonTemporarily(button: View) {
        button.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            button.isEnabled = true
        }, 500) // 0.5 seconds delay
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
                R.id.navigation_find_users -> {
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
