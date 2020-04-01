package com.instana.mobileeum.ui.performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.instana.mobileeum.R
import kotlinx.android.synthetic.main.fragment_performance.view.*

class PerformanceFragment : Fragment() {

    private lateinit var viewModel: PerformanceViewModel
    private var waitingResponse = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PerformanceViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_performance, container, false)

        // Actions
        root.lowMemory.setOnClickListener {
            waitingResponse = true
            viewModel.forceLowMemory(requireActivity().application)
        }
        root.frameSkip.setOnClickListener {
            waitingResponse = true
            viewModel.forceFrameSkip()
        }
        root.anr.setOnClickListener {
            waitingResponse = true
            viewModel.forceANR()
        }

        // Status
        viewModel.response.observe(viewLifecycleOwner, Observer {
            root.status.text = it
            if (waitingResponse) root.scrollView.fullScroll(View.FOCUS_DOWN)
        })

        return root
    }
}
