package com.example.nearbymosque.Fragments

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.nearbymosque.Object.Cancel
import com.example.nearbymosque.databinding.FragmentNotificationBinding

class NotificationFragment : Fragment() {
    private val binding by lazy { FragmentNotificationBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.gifImageView.visibility=View.VISIBLE

        binding.webView.settings.javaScriptEnabled=true
        binding.webView.webViewClient=object :WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.gifImageView.visibility=View.GONE
            }
        }

        val url="https://islom.uz/maqolalar/51/1"
        binding.webView.loadUrl(url)
        binding.webView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
                binding.webView.goBack()
                true
            } else {
                false
            }
        }




        return binding.root
    }

}
