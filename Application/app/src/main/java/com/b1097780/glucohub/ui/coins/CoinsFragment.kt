package com.b1097780.glucohub.ui.coins

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.b1097780.glucohub.MainActivity
import com.b1097780.glucohub.R
import com.b1097780.glucohub.PreferencesHelper

class CoinsFragment : Fragment() {

    private lateinit var textCoins: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_coins, container, false)

        // Set shop title and subtitle
        root.findViewById<TextView>(R.id.text_shop_subtitle).text = "New items every week"

        // Show current coins
        textCoins = root.findViewById(R.id.text_coins_title)
        updateCoinDisplay()

        // Setup sections
        setupButtons(root.findViewById(R.id.container_themes), listOf("Default", "Spooky", "Bubblegum"), 200)
        setupButtons(root.findViewById(R.id.container_mottos), listOf("Sugar Monster", "Fitness Fanatic", "Gluco Warrior"), 100)
        setupButtons(root.findViewById(R.id.container_colors), listOf("Red", "Blue", "Pink"), 150)
        setupImageButtons(root.findViewById(R.id.container_pictures), listOf(
            R.drawable.profile_smiley, R.drawable.profile_star, R.drawable.profile_lightning
        ), 300)

        // Setup discount buttons (confirmation added)
        setupDiscountButton(root.findViewById(R.id.button_glucose_stickers))
        setupDiscountButton(root.findViewById(R.id.button_dexcom_stickers))

        return root
    }

    private fun setupButtons(container: LinearLayout, buttonLabels: List<String>, cost: Int) {
        container.removeAllViews()

        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
        }

        buttonLabels.forEach { label ->
            val button = Button(requireContext()).apply {
                text = label
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply { setMargins(16, 8, 16, 8) }

                // Set colors based on category
                if (container.id == R.id.container_mottos) {
                    setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
                    setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
                } else {
                    when (label) {
                        "Default" -> setBackgroundColor(android.graphics.Color.parseColor("#38aeee"))
                        "Spooky" -> setBackgroundColor(android.graphics.Color.parseColor("#39271A"))
                        "Bubblegum" -> setBackgroundColor(android.graphics.Color.parseColor("#9306A5"))
                        "Red" -> setBackgroundColor(android.graphics.Color.parseColor("#F24A75"))
                        "Blue" -> setBackgroundColor(android.graphics.Color.parseColor("#6475FC"))
                        "Pink" -> setBackgroundColor(android.graphics.Color.parseColor("#F2A6D5"))
                    }
                }

                setOnClickListener {
                    applyClickAnimation(it)

                    val currentCoins = PreferencesHelper.getUserCoins(requireContext())

                    // Prevent re-buying if the user already has the selected item
                    if (container.id == R.id.container_themes && PreferencesHelper.getUserTheme(requireContext()) == label.lowercase()) {
                        showToast("You already have this theme!")
                        return@setOnClickListener
                    }
                    if (container.id == R.id.container_mottos && PreferencesHelper.getUserMotto(requireContext()) == label) {
                        showToast("You already have this motto!")
                        return@setOnClickListener
                    }
                    if (container.id == R.id.container_colors && PreferencesHelper.getBoxColor(requireContext()) == getColorForLabel(label)) {
                        showToast("You already have this color!")
                        return@setOnClickListener
                    }

                    if (currentCoins < cost) {
                        showToast("Not enough coins!")
                        return@setOnClickListener
                    }

                    // Ask for confirmation before spending coins
                    showConfirmationDialog("Are you sure you want to spend $cost coins on $label?") {
                        PreferencesHelper.setUserCoins(requireContext(), currentCoins - cost)
                        updateCoinDisplay()

                        if (container.id == R.id.container_themes) {
                            val newTheme = label.lowercase()
                            PreferencesHelper.setUserTheme(requireContext(), newTheme)
                            (requireActivity() as MainActivity).applyUserTheme(newTheme)
                            (requireActivity() as MainActivity).reloadTheme()
                            showToast("New theme applied!")

                        } else if (container.id == R.id.container_mottos) {
                            PreferencesHelper.setUserMotto(requireContext(), label)
                            (requireActivity() as MainActivity).reloadTheme()
                            showToast("New motto applied!")

                        } else if (container.id == R.id.container_colors) {
                            PreferencesHelper.setBoxColor(requireContext(), getColorForLabel(label))
                            (requireActivity() as MainActivity).reloadTheme()
                            showToast("New color applied!")
                        }
                    }
                }
            }
            row.addView(button)
        }

        container.addView(row)
    }
    private fun getColorForLabel(label: String): String {
        return when (label) {
            "Red" -> "#F24A75"
            "Blue" -> "#6475FC"
            "Pink" -> "#F2A6D5"
            else -> "#FFFFFF"
        }
    }

    private fun updateCoinDisplay() {
        val coins = PreferencesHelper.getUserCoins(requireContext())
        textCoins.text = "Your Coins: $coins"
    }

    private fun setupDiscountButton(button: Button) {
        button.setOnClickListener {
            applyClickAnimation(it)

            val currentCoins = PreferencesHelper.getUserCoins(requireContext())

            if (currentCoins < 1500) {
                showToast("Not enough coins!")
                return@setOnClickListener
            }

            // Generate a random discount code
            val discountCodes = listOf("GLUCO10", "DIABETES15", "SUGARFREE20", "HEALTH30")
            val randomCode = discountCodes.random()

            // Ask for confirmation before spending 1500 coins on a discount code
            showConfirmationDialog("Are you sure you want to spend 1500 coins to redeem this discount code?") {
                // Deduct coins and update UI
                PreferencesHelper.setUserCoins(requireContext(), currentCoins - 1500)
                updateCoinDisplay()

                // Update button text and disable it
                button.text = "Your Discount Code: $randomCode"
                button.isEnabled = false
                showToast("Discount Code: $randomCode")
            }
        }
    }


    private fun setupImageButtons(container: LinearLayout, imageResources: List<Int>, cost: Int) {
        container.removeAllViews()

        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        imageResources.forEach { imageRes ->
            val button = Button(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, 150, 1f
                ).apply { setMargins(16, 8, 16, 8) }
                setBackgroundResource(imageRes)
                text = ""

                setOnClickListener {
                    applyClickAnimation(it)

                    val currentCoins = PreferencesHelper.getUserCoins(requireContext())

                    if (PreferencesHelper.getProfilePicture(requireContext()) == imageRes.toString()) {
                        showToast("You already have this profile picture!")
                        return@setOnClickListener
                    }

                    if (currentCoins < cost) {
                        showToast("Not enough coins!")
                        return@setOnClickListener
                    }

                    // Ask for confirmation before buying the profile picture
                    showConfirmationDialog("Are you sure you want to spend $cost coins on this profile picture?") {
                        PreferencesHelper.setUserCoins(requireContext(), currentCoins - cost)
                        updateCoinDisplay()

                        PreferencesHelper.setProfilePicture(requireContext(), imageRes.toString())
                        showToast("New profile picture applied!")

                        // Disable the button after selection
                        it.isEnabled = false
                    }
                }
            }
            row.addView(button)
        }

        container.addView(row)
    }

    private fun showConfirmationDialog(message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Purchase")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun applyClickAnimation(view: View) {
        val shrinkAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button)
        view.startAnimation(shrinkAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            println("Button Click Animation Completed")
        }, 150)
    }

    private fun getThemeColor(context: android.content.Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
