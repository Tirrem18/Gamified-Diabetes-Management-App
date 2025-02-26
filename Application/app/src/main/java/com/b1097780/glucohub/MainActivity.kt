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
import com.github.mikephil.charting.data.Entry
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
        //sharedPreferences.edit().clear().apply()
        userCoins = sharedPreferences.getInt("userCoins", 10) // Default to 10 coins if not set

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
            "purple" -> setTheme(R.style.Theme_GlucoHub_purple)
            "grey" -> setTheme(R.style.Theme_GlucoHub_grey)
            "pink" -> setTheme(R.style.Theme_GlucoHub_pink)
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

    fun saveGlucoseEntries(entries: List<Entry>) {
        val editor = sharedPrefs.edit()
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val entryString = entries.joinToString(";") { entry ->
            "$today,${entry.x},${entry.y}" // Save date along with time and level
        }

        editor.putString("glucoseEntries", entryString)
        editor.apply()
    }


    fun loadGlucoseEntries(): List<Entry> {
        val entryString = sharedPrefs.getString("glucoseEntries", "") ?: ""
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        return entryString.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 3 && parts[0] == today) {
                Entry(parts[1].toFloat(), parts[2].toFloat()) // Only return today's entries
            } else {
                null
            }
        }
    }



    fun saveLastEntryTime(time: Long) {
        sharedPrefs.edit().putLong("lastEntryTime", time).apply()
    }

    fun loadLastEntryTime(): Long {
        return sharedPrefs.getLong("lastEntryTime", 0L)
    }
}
