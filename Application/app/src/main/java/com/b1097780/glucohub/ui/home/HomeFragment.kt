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
import com.b1097780.glucohub.PreferencesHelper
import com.b1097780.glucohub.R
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogDialog
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogEntry
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogViewModel
import com.b1097780.glucohub.ui.home.GlucoseGraph.GraphViewModel
import com.b1097780.glucohub.ui.friends.FriendsViewModel
import com.b1097780.glucohub.ui.home.GraphDialog.GraphDialog
import com.github.mikephil.charting.data.Entry
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var graphViewModel: GraphViewModel
    private lateinit var activityLogViewModel: ActivityLogViewModel
    private var lastEntryTime: Long = 0

    // ✅ Lifecycle Method: Create View & Initialize ViewModels
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        graphViewModel = ViewModelProvider(requireActivity()).get(GraphViewModel::class.java)
        activityLogViewModel = ViewModelProvider(requireActivity()).get(ActivityLogViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupButtons()
        setupObservers()

        // ✅ Load saved last entry time from SharedPreferences
        lastEntryTime = PreferencesHelper.getLastEntryTime(requireContext())

        // ✅ Load recent blood glucose entry & activity logs only once
        if (savedInstanceState == null) {
            activityLogViewModel.loadRecentBloodEntry(requireContext())
            activityLogViewModel.loadActivityEntries(requireContext())
        }

        return binding.root
    }

    // ✅ Lifecycle Method: Refresh data when the fragment resumes
    override fun onResume() {
        super.onResume()
        activityLogViewModel.loadRecentBloodEntry(requireContext())
        activityLogViewModel.loadActivityEntries(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ✅ Set up UI observers to update text dynamically
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
            disableButtonTemporarily(it)

            val currentTime = System.currentTimeMillis()

            // ✅ Prevent duplicate glucose entries within 5 minutes
            if ((currentTime - lastEntryTime) < 5 * 60 * 1000) {
                AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                    .setTitle("Wait before entering again")
                    .setMessage("Please wait at least 5 minutes before entering another glucose level.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
                return@setOnClickListener
            }

            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button))
            Handler(Looper.getMainLooper()).postDelayed({
                showGlucoseInputPopup()
            }, 200)
        }

        binding.button2.setOnClickListener {
            disableButtonTemporarily(it)

            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button))
            Handler(Looper.getMainLooper()).postDelayed({
                showActivityInputPopup()
            }, 200)
        }
    }

    // ✅ Function to disable button for 0.5 seconds
    private fun disableButtonTemporarily(button: View) {
        button.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            button.isEnabled = true
        }, 500) // 0.5 seconds delay
    }


    // ✅ Show popup for glucose input
    private fun showGlucoseInputPopup() {
        GraphDialog(requireContext()) { time, glucoseLevel ->
            processGlucoseEntry(time, glucoseLevel)
        }.show()
    }

    // ✅ Show popup for activity input
    private fun showActivityInputPopup() {
        ActivityLogDialog(requireContext()) { activity, startTime, endTime, description ->
            if (startTime != null) {
                processActivityEntry(activity, startTime, endTime, description)
            }
        }.show()
    }

    // ✅ Get current time in hours + minutes (decimal format)
    private fun getCurrentTime(): Float {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toFloat()
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE) / 60f
        return currentHour + currentMinute
    }

    // ✅ Process and save glucose entry
    private fun processGlucoseEntry(currentTime: Float, number: Float) {
        (activity as? MainActivity)?.let { mainActivity ->
            graphViewModel.addGlucoseEntry(Entry(currentTime, number), mainActivity)
        }

        // ✅ Save last entry time in SharedPreferences
        lastEntryTime = System.currentTimeMillis()
        PreferencesHelper.setLastEntryTime(requireContext(), lastEntryTime)

        // ✅ Refresh recent blood glucose entry
        activityLogViewModel.loadRecentBloodEntry(requireContext())

        // ✅ Coin reward logic
        val friendsViewModel = ViewModelProvider(requireActivity())[FriendsViewModel::class.java]
        friendsViewModel.coinMultiplier.removeObservers(viewLifecycleOwner)
        friendsViewModel.coinMultiplier.observe(viewLifecycleOwner) { multiplier ->
            val coinsEarned = 1 * multiplier
            (activity as? MainActivity)?.addCoinsFromFragment(coinsEarned)

            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                .setTitle("Coins Earned!")
                .setMessage("You've earned $coinsEarned coins!")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    // ✅ Process and save activity entry
    private fun processActivityEntry(activity: String, startTime: String, endTime: String?, description: String) {
        val entry = ActivityLogEntry(activity, startTime, endTime, description)
        requireActivity().let {
            activityLogViewModel.addActivityEntry(entry, it)
        }

        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Activity Logged")
            .setMessage("You've successfully logged: $activity at $startTime.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()

        // ✅ Coin reward logic
        val friendsViewModel = ViewModelProvider(requireActivity())[FriendsViewModel::class.java]
        friendsViewModel.coinMultiplier.removeObservers(viewLifecycleOwner)
        friendsViewModel.coinMultiplier.observe(viewLifecycleOwner) { multiplier ->
            val coinsEarned = 1 * multiplier
            (activity as? MainActivity)?.addCoinsFromFragment(coinsEarned)

            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                .setTitle("Coins Earned!")
                .setMessage("You've earned $coinsEarned coins!")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

    }


    // ✅ Function to show confirmation dialogs for extreme glucose levels
    private fun showWarningDialog(
        title: String,
        message: String,
        currentTime: Float,
        number: Float,
        parentDialog: AlertDialog
    ) {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ ->
                processGlucoseEntry(currentTime, number)
                parentDialog.dismiss()
            }
            .setNegativeButton("Cancel") { warnDialog, _ -> warnDialog.dismiss() }
            .show()
    }

}
