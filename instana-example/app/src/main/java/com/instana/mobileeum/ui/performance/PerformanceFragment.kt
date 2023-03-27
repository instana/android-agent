/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.instana.mobileeum.databinding.FragmentPerformanceBinding

class PerformanceFragment : Fragment() {

    private lateinit var viewModel: PerformanceViewModel
    private var waitingResponse = false

    private var _binding: FragmentPerformanceBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PerformanceViewModel::class.java)
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        val root = binding.root

        // Actions
        binding.lowMemory.setOnClickListener {
            waitingResponse = true
            viewModel.forceLowMemory(requireActivity().application)
        }
        binding.frameSkip.setOnClickListener {
            waitingResponse = true
            viewModel.forceFrameSkip()
        }
        binding.anr.setOnClickListener {
            waitingResponse = true
            viewModel.forceANR()
        }
        binding.unhandledException.setOnClickListener {
            waitingResponse = true
            viewModel.forceUnhandledException()
        }
        binding.unhandledExceptionRunBlocking.setOnClickListener {
            waitingResponse = true
            viewModel.forceUnhandledExceptionInCoroutineBlocking()
        }
        binding.unhandledExceptionGlobalScope.setOnClickListener {
            waitingResponse = true
            viewModel.forceUnhandledExceptionInGlobalScope()
        }
        binding.unhandledExceptionMainLooper.setOnClickListener {
            waitingResponse = true
            viewModel.forceUnhandledExceptionInMainLooper()
        }

        // Status
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
