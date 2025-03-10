package com.b1097780.glucohub.ui.dailylogs

import CalendarFragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import com.b1097780.glucohub.R


class DailyLogsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root = inflater.inflate(R.layout.fragment_daily_logs, container, false)

        // Load CalendarFragment dynamically
        childFragmentManager.beginTransaction()
            .replace(R.id.calendar_fragment_container, CalendarFragment())
            .commit()

        return root
    }
}
