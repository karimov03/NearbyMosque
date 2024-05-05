package com.example.nearbymosque.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nearbymosque.Object.Cancel
import com.example.nearbymosque.R
import com.example.nearbymosque.databinding.FragmentNotificationBinding
import com.example.nearbymosque.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private val binding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }
}