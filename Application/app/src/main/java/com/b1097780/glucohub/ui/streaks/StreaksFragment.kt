package com.b1097780.glucohub.ui.streaks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.MainActivity
import com.b1097780.glucohub.PreferencesHelper
import com.b1097780.glucohub.R

class StreaksFragment : Fragment() {

    private lateinit var viewModel: StreaksViewModel
    private lateinit var textCurrentStreak: TextView
    private lateinit var textHighestStreak: TextView
    private lateinit var textMultiplier: TextView
    private lateinit var textMultiplierInfo: TextView
    private lateinit var progressMultiplier: ProgressBar

    private lateinit var btnClaim7: Button
    private lateinit var btnClaim30: Button
    private lateinit var btnClaim90: Button
    private lateinit var btnClaim365: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_streaks, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(StreaksViewModel::class.java)

        // Initialize UI Elements
        textCurrentStreak = root.findViewById(R.id.text_current_streak)
        textHighestStreak = root.findViewById(R.id.text_highest_streak)
        textMultiplier = root.findViewById(R.id.text_multiplier_progress)
        textMultiplierInfo = root.findViewById(R.id.text_multiplier_info)
        progressMultiplier = root.findViewById(R.id.progress_multiplier)

        btnClaim7 = root.findViewById(R.id.btn_claim_7)
        btnClaim30 = root.findViewById(R.id.btn_claim_30)
        btnClaim90 = root.findViewById(R.id.btn_claim_90)
        btnClaim365 = root.findViewById(R.id.btn_claim_365)

        // Observe LiveData from ViewModel
        viewModel.currentStreak.observe(viewLifecycleOwner) { textCurrentStreak.text = it }
        viewModel.highestStreak.observe(viewLifecycleOwner) { textHighestStreak.text = it }
        viewModel.multiplierText.observe(viewLifecycleOwner) { textMultiplier.text = it }

        updateUI()

        return root
    }

    private fun updateUI() {
        val context = requireContext()

        // Update highest streak & coin multiplier
        PreferencesHelper.updateHighestStreak(context)
        PreferencesHelper.updateCoinMultiplier(context)

        viewModel.loadData(context)

        val currentStreak = PreferencesHelper.getUserStreak(context)
        val multiplier = PreferencesHelper.getCoinMultiplier(context)

        // Update Multiplier Progress
        progressMultiplier.progress = (currentStreak.coerceAtMost(100))
        textMultiplier.text = "Your Current Multiplier Progress: x$multiplier"
        textMultiplierInfo.text = "(The higher your streak, the higher your coin multiplier)"

        // Enable claim buttons if conditions are met, otherwise mark as claimed
        updateClaimButton(btnClaim7, 7, 25)
        updateClaimButton(btnClaim30, 30, 100)
        updateClaimButton(btnClaim90, 90, 500)
        updateClaimButton(btnClaim365, 365, 1000)
    }

    private fun updateClaimButton(button: Button, days: Int, coins: Int) {
        val context = requireContext()
        if (PreferencesHelper.isMilestoneClaimed(context, days)) {
            button.text = "Claimed"
            button.isEnabled = false
        } else {
            button.isEnabled = PreferencesHelper.getUserStreak(context) >= days
            button.setOnClickListener { claimReward(days, coins) }
        }
    }

    private fun claimReward(days: Int, coins: Int) {
        val context = requireContext()
        PreferencesHelper.setMilestoneClaimed(context, days)
        PreferencesHelper.addCoins(context, coins)

        // 🔥 Ensure UI refreshes immediately
        (activity as? MainActivity)?.updateCoinButton(PreferencesHelper.getUserCoins(context))

        updateUI() // Refresh UI after claiming
    }
}
