package com.b1097780.glucohub

import android.content.Intent
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
import com.b1097780.glucohub.ui.login.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var userCoins: Int = 10
    private var userStreak: Int = 0 // Variable to store the streak
    private var theme = ""

    override fun onCreate(savedInstanceState: Bundle?) {


        PreferencesHelper.setUserStreak(this, 99) // Manually set current streak

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
        // Update UI with loaded coins
        updateCoinButton(userCoins)
        updateStreakButton(userStreak)




    }
    override fun onStart() {
        super.onStart()
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Prevent back navigation to main activity
        }
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
    //Reloads the activity to apply theme changes
    fun reloadTheme() {
        recreate()
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
            navController.popBackStack(R.id.navigation_home, false)

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
        if (::binding.isInitialized) { // ✅ Prevents crashes
            binding.customCoinButton.text = formattedValue
        }
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
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("OK") { _, _ ->
                FirebaseAuth.getInstance().signOut() // Log the user out
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Prevent going back
                startActivity(intent)
                finish() // Close MainActivity
            }
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
