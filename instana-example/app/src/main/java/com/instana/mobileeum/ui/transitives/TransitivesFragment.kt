/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.transitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.instana.mobileeum.databinding.FragmentTransitivesBinding
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class TransitivesFragment : Fragment() {

    private lateinit var viewModel: TransitivesViewModel
    private var waitingResponse = false

    private var _binding: FragmentTransitivesBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(TransitivesViewModel::class.java)
        _binding = FragmentTransitivesBinding.inflate(inflater, container, false)
        val root = binding.root

        // Trigger libraries with traced transitive dependencies
        binding.retrofit.setOnClickListener {
            waitingResponse = true
            viewModel.executeRetrofitRequest()
        }
        binding.picasso.setOnClickListener {
            Picasso.get()
                .load("https://picsum.photos/300/300")
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(binding.picassoImage)
        }
        binding.glide.setOnClickListener {
            Glide.with(this)
                .load("https://picsum.photos/300/300")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.glideImage)
        }

        // Handle asynchronous responses
        viewModel.response.observe(viewLifecycleOwner, Observer {
            binding.status.text = it
            if (waitingResponse) binding.scrollView.fullScroll(View.FOCUS_DOWN)
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
