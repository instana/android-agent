package com.instana.mobileeum.ui.transitives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.instana.mobileeum.R
import kotlinx.android.synthetic.main.fragment_transitives.view.*

class TransitivesFragment : Fragment() {

    private lateinit var viewModel: TransitivesViewModel
    private var waitingResponse = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(TransitivesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_transitives, container, false)

        // Trigger libraries with traced transitive dependencies
        root.retrofit.setOnClickListener {
            waitingResponse = true
            viewModel.executeRetrofitRequest()
        }

        // Status
        viewModel.response.observe(viewLifecycleOwner, Observer {
            root.status.text = it
            if (waitingResponse) root.scrollView.fullScroll(View.FOCUS_DOWN)
        })

        return root
    }
}
