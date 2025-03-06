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
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var userCoins: Int = 25 // Initial coins (or load from database/shared preferences)
    private val sharedPrefs by lazy { getSharedPreferences("GlucoHubPrefs", MODE_PRIVATE) }

    // CUSTOMISE
    private var theme = "" // Change this to "default", "purple", or "plain"

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("GlucoHubPrefs", MODE_PRIVATE)

        userCoins = sharedPreferences.getInt("userCoins", 10) // Default to 10 coins if not set

        applyUserTheme(theme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //sharedPreferences.edit().clear().apply()
        populateTestData()//test

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

    private fun setupCustomButtons() {
        val customMenuButton = findViewById<ImageButton>(R.id.custom_menu_button)
        val customBackButton = findViewById<ImageButton>(R.id.custom_back_button)
        val customStreakButton = findViewById<Button>(R.id.custom_streak_button)
        val customCoinButton = findViewById<Button>(R.id.custom_coin_button)
        val shrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.shrink_button)

        customMenuButton.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper())
                .postDelayed(
                    { binding.drawerLayout.openDrawer(binding.navViewDrawer) },
                    150
                ) // Delay for 200ms
        }

        customBackButton.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper())
                .postDelayed({ onBackPressedDispatcher.onBackPressed() }, 150)
        }

        customStreakButton.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper())
                .postDelayed(
                    {
                        findNavController(R.id.nav_host_fragment_activity_main)
                            .navigate(R.id.navigation_streaks)
                    },
                    150
                )
        }

        customCoinButton.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper())
                .postDelayed(
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

    private fun updateCoinButton(value: Int) {
        val formattedValue = formatNumber(value, false)
        binding.customCoinButton.text = formattedValue
    }

    fun addCoinsFromFragment(amount: Int) {
        addCoins(amount)
    }

    private fun addCoins(amount: Int) {
        userCoins += amount
        updateCoinButton(userCoins)

        val sharedPreferences = getSharedPreferences("GlucoHubPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putInt("userCoins", userCoins).apply()
    }

    private fun updateStreakButton(value: Int) {
        val formattedValue = formatNumber(value, true)
        binding.customStreakButton.text = formattedValue
    }

    private fun formatNumber(value: Int, isStreak: Boolean): String {
        return if (isStreak) {
            // Streak logic: Convert to K+ format if ≥ 1000
            when {
                value > 999 -> "1K+"
                else -> value.toString()
            }
        } else {
            // Coins logic: Exact value unless ≥ 100,000
            when {
                value > 99999 -> "100K+"
                else -> value.toString() // Exact value otherwise
            }
        }
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


    fun saveLastEntryTime(time: Long) {
        sharedPrefs.edit().putLong("lastEntryTime", time).apply()
    }

    fun loadLastEntryTime(): Long {
        return sharedPrefs.getLong("lastEntryTime", 0L)
    }

    private fun populateTestData() {
        val sharedPrefs = getSharedPreferences("GlucoHubPrefs", MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        // Expanded Glucose Test Data (Randomized Intervals)
        val testEntries = listOf(
            "$today,0.0,8.0",  "$today,0.15,7.2", "$today,0.2,6.5",  "$today,0.35,9.1", "$today,0.5,10.0",
            "$today,1.0,5.9", "$today,1.3,6.8", "$today,2.0,7.8", "$today,2.4,8.5", "$today,3.0,12.4",
            "$today,3.6,10.3", "$today,4.0,9.2", "$today,4.3,7.9", "$today,5.0,6.7", "$today,5.4,8.1",
            "$today,6.0,7.1", "$today,6.8,5.9", "$today,7.0,5.5", "$today,7.7,9.5", "$today,8.0,9.0",
            "$today,8.6,10.7", "$today,9.0,10.2", "$today,9.5,6.3", "$today,10.0,6.9", "$today,10.7,8.2",
            "$today,11.0,5.8", "$today,11.3,7.5", "$today,12.0,8.5", "$today,12.6,9.1", "$today,13.0,7.0",
            "$today,13.4,6.5", "$today,14.0,6.2", "$today,14.7,8.3", "$today,15.0,11.3", "$today,15.1,5.6",
            "$today,16.0,7.9", "$today,17.0,9.7", "$today,17.3,8.6", "$today,18.0,7.5", "$today,18.9,9.1",
            "$today,19.0,10.8", "$today,19.5,6.8", "$today,20.0,6.4", "$today,20.8,7.9", "$today,21.0,8.9",
            "$today,21.6,10.1", "$today,22.0,5.7", "$today,22.3,6.4", "$today,23.0,7.2", "$today,23.5,8.0"
        )
        editor.putString("glucoseEntries", testEntries.joinToString(";"))

        // Expanded Activity Log Test Data (Random 10-30 min intervals from 00:30 to 11:30)
        val testActivities = listOf(
            "$today,Study,00:30,00:50,Reading notes",
            "$today,Coding,01:10,01:35,Fixing bugs",
            "$today,Email,02:00,02:15,Checking inbox",
            "$today,Snack,02:30,02:45,Eating fruit",
            "$today,Walk,03:20,03:50,Morning walk",
            "$today,Music,04:15,04:35,Listening",
            "$today,Game,05:00,05:30,Short break",
            "$today,Plan,05:45,06:00,Daily plan",
            "$today,Write,06:20,06:45,Writing blog",
            "$today,Coffee,07:10,07:30,Espresso time",
            "$today,News,08:00,08:20,Reading news",
            "$today,Stretch,08:40,09:00,Morning yoga",
            "$today,Work,09:30,10:00,Focus task",
            "$today,Meet,10:15,10:45,Team meeting",
            "$today,Call,11:10,11:30,Client call",
            "$today,Lunch,12:30,13:00,Meal time",
            "$today,Walk,13:30,14:00,Afternoon walk",
            "$today,Gym,15:00,16:00,Strength train",
            "$today,Dinner,20:00,20:30,Healthy meal",
            "$today,Read,21:30,22:00,Reading book"
        )

        // Convert list to a correctly formatted string and save in SharedPreferences
        val formattedActivities = testActivities.joinToString(";")
        editor.putString("activityLogEntries", formattedActivities)

        editor.apply() // Save data
    }







}
