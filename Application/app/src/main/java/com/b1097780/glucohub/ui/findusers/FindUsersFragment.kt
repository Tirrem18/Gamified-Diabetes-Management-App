package com.b1097780.glucohub.ui.findusers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.databinding.FragmentFindUsersBinding // ✅ Correct import

class FindUsersFragment : Fragment() {

    private var _binding: FragmentFindUsersBinding? = null // ✅ Correct class name
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val findUsersViewModel =
            ViewModelProvider(this).get(FindUsersViewModel::class.java)

        _binding = FragmentFindUsersBinding.inflate(inflater, container, false) // ✅ Fix here
        val root: View = binding.root

        val textView: TextView = binding.textProfile
        findUsersViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
