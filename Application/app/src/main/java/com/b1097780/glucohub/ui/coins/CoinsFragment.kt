package com.b1097780.glucohub.ui.coins

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
        root.findViewById<TextView>(R.id.text_shop_title).text = "This Week's Shop"
        root.findViewById<TextView>(R.id.text_shop_subtitle).text = "New items every week"

        // Show current coins
        textCoins = root.findViewById(R.id.text_coins_title)
        updateCoinDisplay()

        // Setup sections
        setupButtons(root.findViewById(R.id.container_themes), listOf("Theme A", "Theme B", "Theme C"))
        setupButtons(root.findViewById(R.id.container_mottos), listOf("Motto 1", "Motto 2", "Motto 3"))
        setupButtons(root.findViewById(R.id.container_colors), listOf("Color 1", "Color 2", "Color 3"))
        setupImageButtons(root.findViewById(R.id.container_pictures), listOf(
            R.drawable.profile_smiley, R.drawable.profile_star, R.drawable.profile_lightning // No Heart
        ))

        return root
    }

    private fun updateCoinDisplay() {
        val coins = PreferencesHelper.getUserCoins(requireContext())
        textCoins.text = "Your Coins: $coins"
    }

    private fun setupButtons(container: LinearLayout, buttonLabels: List<String>) {
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
            val button = Button(requireContext()).apply { text = label }
            row.addView(button)
        }
        container.addView(row)
    }

    private fun setupImageButtons(container: LinearLayout, imageResources: List<Int>) {
        container.removeAllViews()

        // Create a background container for profile pictures
        val backgroundContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.attr.textColorPrimary)) // Set background color
            setPadding(16, 16, 16, 16)
        }

        imageResources.forEach { imageRes ->
            val button = Button(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, 150, 1f  // Adjust height to fit better
                ).apply { setMargins(8, 8, 8, 8) }
                setBackgroundResource(imageRes)
            }
            backgroundContainer.addView(button)
        }

        container.addView(backgroundContainer)
    }
}
