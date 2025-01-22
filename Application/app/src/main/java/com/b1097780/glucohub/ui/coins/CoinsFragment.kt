package com.b1097780.glucohub.ui.coins

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.databinding.FragmentCoinsBinding

class CoinsFragment : Fragment() {

    private var _binding: FragmentCoinsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val coinsViewModel =
            ViewModelProvider(this).get(CoinsViewModel::class.java)

        _binding = FragmentCoinsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textCoins
        coinsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}