/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.custom

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
import com.instana.mobileeum.databinding.FragmentCustomBinding
import com.instana.mobileeum.notBlankOrNull
import com.instana.mobileeum.toMap

class CustomEventFragment : Fragment() {

    private lateinit var viewModel: CustomEventViewModel

    private var presets = emptyList<RequestPreset>()
    private var waitingResponse = false

    private var textChangeListener: TextWatcher? = null

    private var _binding: FragmentCustomBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(CustomEventViewModel::class.java)
        _binding = FragmentCustomBinding.inflate(inflater, container, false)
        val root = binding.root

        // Request Presets
        viewModel.presets.observe(viewLifecycleOwner, Observer { list ->
            presets = list
            val spinnerOptions = list.map { it.presetName }.toMutableList().apply { add("CUSTOM") }
            binding.eventPresets.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, spinnerOptions)
        })
        binding.eventPresets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                presets.getOrNull(position)?.run {
                    listenEditTextChanges(false)
                    binding.eventName.setText(this.eventName)
                    binding.eventStartTime.setText(this.startTime?.toString())
                    binding.eventDuration.setText(this.duration?.toString())
                    binding.eventViewName.setText(this.viewName)
                    binding.eventMeta.setText(this.meta?.toString())
                    binding.eventBackendTracingId.setText(this.backendTracingId)
                    binding.eventError.setText(this.error)
                    listenEditTextChanges(true)
                }
            }
        }

        // EditTexts
        listenEditTextChanges(true)

        // Run Button
        binding.sendEvent.setOnClickListener {
            waitingResponse = true
            viewModel.sendCustomEvent(
                eventName = binding.eventName.text.toString(),
                startTimeEpochMs = binding.eventStartTime.text?.toString()?.notBlankOrNull()?.toLong(),
                durationMs = binding.eventDuration.text?.toString()?.notBlankOrNull()?.toLong(),
                viewName = binding.eventViewName.text?.toString()?.notBlankOrNull(),
                meta = binding.eventMeta.text?.toString()?.notBlankOrNull()?.toMap(),
                backendTracingID = binding.eventBackendTracingId.text?.toString()?.notBlankOrNull(),
                errorMessage = binding.eventError.text?.toString()?.notBlankOrNull()
            )
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun listenEditTextChanges(enabled: Boolean) {
        val textInputs = listOf(
            binding.eventName,
            binding.eventStartTime,
            binding.eventDuration,
            binding.eventViewName,
            binding.eventMeta,
            binding.eventBackendTracingId,
            binding.eventError
        )
        textChangeListener?.run {
            textInputs.forEach { it.removeTextChangedListener(this) }
        }
        if (enabled) {
            textChangeListener = object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val textInputFocused = textInputs.any { it.isFocused }
                    if (textInputFocused) {
                        binding.eventPresets.setSelection(presets.size)
                    }
                }
            }
            textInputs.forEach { it.addTextChangedListener(textChangeListener) }
        }
    }
}
