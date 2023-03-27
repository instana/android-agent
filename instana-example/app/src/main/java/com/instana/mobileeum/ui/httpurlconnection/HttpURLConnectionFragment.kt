/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.httpurlconnection

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.instana.mobileeum.databinding.FragmentHttpurlconnectionBinding
import com.instana.mobileeum.network.RequestPreset

class HttpURLConnectionFragment : Fragment() {

    private lateinit var viewModel: HttpURLConnectionViewModel

    private var presets = emptyList<RequestPreset>()
    private var waitingResponse = false

    private var textChangeListener: TextWatcher? = null

    private var _binding: FragmentHttpurlconnectionBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(HttpURLConnectionViewModel::class.java)
        _binding = FragmentHttpurlconnectionBinding.inflate(inflater, container, false)
        val root = binding.root

        // Request Presets
        viewModel.presets.observe(viewLifecycleOwner, Observer { list ->
            presets = list
            val spinnerOptions = list.map { it.name }.toMutableList().apply { add("CUSTOM") }
            binding.requestPresets.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, spinnerOptions)
        })
        binding.requestPresets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                presets.getOrNull(position)?.run {
                    listenEditTextChanges(false)
                    binding.requestMethod.setText(this.method)
                    binding.requestUrl.setText(this.url)
                    binding.requestBody.setText(this.body)
                    listenEditTextChanges(true)
                }
            }
        }

        // EditTexts
        listenEditTextChanges(true)

        // Run Button
        binding.runRequest.setOnClickListener {
            waitingResponse = true
            viewModel.executeRequest(
                method = binding.requestMethod.text.toString(),
                url = binding.requestUrl.text.toString(),
                body = if (binding.requestBody.text.isNullOrBlank()) null else binding.requestBody.text.toString()
            )
        }

        // Response
        viewModel.response.observe(viewLifecycleOwner, Observer {
            binding.response.text = it
            if (waitingResponse) binding.scrollView.fullScroll(View.FOCUS_DOWN)
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun listenEditTextChanges(enabled: Boolean) {
        textChangeListener?.run {
            binding.requestMethod.removeTextChangedListener(this)
            binding.requestUrl.removeTextChangedListener(this)
            binding.requestBody.removeTextChangedListener(this)
        }
        if (enabled) {
            textChangeListener = object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (binding.requestMethod.isFocused || binding.requestUrl.isFocused || binding.requestBody.isFocused) {
                        binding.requestPresets.setSelection(presets.size)
                    }
                }
            }
            binding.requestMethod.addTextChangedListener(textChangeListener)
            binding.requestUrl.addTextChangedListener(textChangeListener)
            binding.requestBody.addTextChangedListener(textChangeListener)
        }
    }
}
