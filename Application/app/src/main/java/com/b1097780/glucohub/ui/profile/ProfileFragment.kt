package com.b1097780.glucohub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize ViewModel
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Observe LiveData and set values to UI
        profileViewModel.profileTitle.observe(viewLifecycleOwner) {
            binding.username.text = it
        }
        profileViewModel.sampleText.observe(viewLifecycleOwner) {
            binding.sampleText.text = it
        }
        profileViewModel.glucoseEntries.observe(viewLifecycleOwner) {
            binding.glucoseEntries.text = it
        }
        profileViewModel.activityEntries.observe(viewLifecycleOwner) {
            binding.activityEntries.text = it
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
