package com.b1097780.glucohub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.databinding.FragmentHomeBinding
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.b1097780.glucohub.MainActivity
import com.b1097780.glucohub.R
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogViewModel
import com.b1097780.glucohub.ui.home.GlucoseGraph.GraphViewModel
import com.b1097780.glucohub.ui.profile.ProfileViewModel
import com.github.mikephil.charting.data.Entry
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var graphViewModel: GraphViewModel
    private lateinit var activityLogViewModel: ActivityLogViewModel
    private var lastEntryTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        graphViewModel = ViewModelProvider(requireActivity()).get(GraphViewModel::class.java)
        activityLogViewModel = ViewModelProvider(requireActivity()).get(ActivityLogViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupButtons()
        setupObservers()

        // Load last entry time
        lastEntryTime = (activity as? MainActivity)?.loadLastEntryTime() ?: 0L
        activityLogViewModel.loadRecentBloodEntry(requireContext())


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        homeViewModel.glucoseText.observe(viewLifecycleOwner) {
            binding.textGlucose.text = it
        }

        homeViewModel.plannerText.observe(viewLifecycleOwner) {
            binding.textActivityLog.text = it
        }

    }


    private fun setupButtons() {
        binding.button1.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            if ((currentTime - lastEntryTime) < 4.4 * 60 * 1000) { // 10 minutes in milliseconds
                AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                    .setTitle("Wait before entering again")
                    .setMessage("Please wait at least 5 minutes since entering your last blood glucose.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
                return@setOnClickListener
            }

            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button))
            Handler(Looper.getMainLooper()).postDelayed({
                showNumberInputPopup()
            }, 200)
        }

        binding.button2.setOnClickListener {

            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button))
            Handler(Looper.getMainLooper()).postDelayed({
                showActivityInputPopup()
            }, 200)
        }
    }

    private fun showNumberInputPopup() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Enter Glucose Level")
            .setMessage("Please enter your current glucose level:")
            .setView(editText)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val inputText = editText.text.toString()
                val number = inputText.toFloatOrNull()

                if (number == null || number < 0f || number > 40f) {
                    editText.error = "Invalid value. Enter a number between 0 and 40."
                    return@setOnClickListener
                }

                val currentTime = getCurrentTime()

                when {
                    number > 25f -> {
                        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                            .setTitle("High Glucose Alert")
                            .setMessage("A glucose level above 25 is dangerously high. Are you sure?")
                            .setPositiveButton("Confirm") { _, _ ->
                                processGlucoseEntry(currentTime, number)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { warnDialog, _ -> warnDialog.dismiss() }
                            .show()
                    }
                    number < 2f -> {
                        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                            .setTitle("Low Glucose Warning")
                            .setMessage("A glucose level below 2 is critically low. Are you sure?")
                            .setPositiveButton("Confirm") { _, _ ->
                                processGlucoseEntry(currentTime, number)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { warnDialog, _ -> warnDialog.dismiss() }
                            .show()
                    }
                    else -> {
                        processGlucoseEntry(currentTime, number)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showActivityInputPopup() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT // Accepts regular text input
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Enter Activity")
            .setMessage("Please enter the activity you just completed:")
            .setView(editText)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val inputText = editText.text.toString().trim()

                if (inputText.isEmpty()) {
                    editText.error = "Activity cannot be empty."
                    return@setOnClickListener
                }

                // 🔥 Placeholder for saving or processing the activity (next step)
                //processActivityEntry(inputText)

                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun getCurrentTime(): Float {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toFloat()
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE) / 60f
        return currentHour + currentMinute
    }

    private fun processGlucoseEntry(currentTime: Float, number: Float) {
        (activity as? MainActivity)?.let { mainActivity ->
            graphViewModel.addGlucoseEntry(Entry(currentTime, number), mainActivity) // ✅ Save to SharedPreferences
        }

        lastEntryTime = System.currentTimeMillis() // ✅ Updates last entry time
        (activity as? MainActivity)?.saveLastEntryTime(lastEntryTime)

        // ✅ Notify Activity Log to refresh its data
        activityLogViewModel.loadRecentBloodEntry(requireContext())

        val profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.coinMultiplier.observe(viewLifecycleOwner) { multiplier ->
            val coinsEarned = 1 * multiplier
            (activity as? MainActivity)?.addCoinsFromFragment(coinsEarned)

            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                .setTitle("Coins Earned!")
                .setMessage("You've earned $coinsEarned coins!")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

}
