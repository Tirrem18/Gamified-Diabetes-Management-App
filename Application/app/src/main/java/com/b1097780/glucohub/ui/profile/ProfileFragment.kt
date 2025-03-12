package com.b1097780.glucohub.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.b1097780.glucohub.PreferencesHelper
import com.b1097780.glucohub.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val context = requireContext()
        profileViewModel = ProfileViewModel(context)

        profileViewModel.profileTitle.observe(viewLifecycleOwner) {
            binding.username.text = it
        }
        profileViewModel.userMotto.observe(viewLifecycleOwner) {
            binding.userMotto.text = it
        }
        profileViewModel.glucoseEntries.observe(viewLifecycleOwner) {
            binding.glucoseEntries.text = it
        }
        profileViewModel.activityEntries.observe(viewLifecycleOwner) {
            binding.activityEntries.text = it
        }
        // ✅ Ensure streak updates before observing
        PreferencesHelper.updateHighestStreak(requireContext())

        profileViewModel.highestStreak.observe(viewLifecycleOwner) {
            binding.highestStreak.text = it
        }

        profileViewModel.joiningDate.observe(viewLifecycleOwner) {
            binding.joiningDate.text = it
        }

        // ✅ Observe dynamically calculated achievements and descriptions
        profileViewModel.activityAchievement.observe(viewLifecycleOwner) {
            binding.achievement1.text = it.first
            binding.achievement1Desc.text = it.second
        }
        profileViewModel.glucoseAchievement.observe(viewLifecycleOwner) {
            binding.achievement2.text = it.first
            binding.achievement2Desc.text = it.second
        }
        profileViewModel.streakAchievement.observe(viewLifecycleOwner) {
            binding.achievement3.text = it.first
            binding.achievement3Desc.text = it.second
        }

        return binding.root
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
