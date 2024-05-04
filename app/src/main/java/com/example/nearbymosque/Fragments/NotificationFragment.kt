package com.example.nearbymosque.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nearbymosque.R
import com.example.nearbymosque.databinding.FragmentNotificationBinding

class NotificationFragment : Fragment() {
    private val binding by lazy { FragmentNotificationBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

}