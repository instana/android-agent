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
import com.instana.mobileeum.R
import com.instana.mobileeum.notBlankOrNull
import com.instana.mobileeum.toMap
import kotlinx.android.synthetic.main.fragment_custom.view.*

class CustomEventFragment : Fragment() {

    private lateinit var viewModel: CustomEventViewModel

    private var presets = emptyList<RequestPreset>()
    private var waitingResponse = false

    private var textChangeListener: TextWatcher? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(CustomEventViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_custom, container, false)

        // Request Presets
        viewModel.presets.observe(viewLifecycleOwner, Observer { list ->
            presets = list
            val spinnerOptions = list.map { it.presetName }.toMutableList().apply { add("CUSTOM") }
            root.eventPresets.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, spinnerOptions)
        })
        root.eventPresets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                presets.getOrNull(position)?.run {
                    listenEditTextChanges(root, false)
                    root.eventName.setText(this.eventName)
                    root.eventStartTime.setText(this.startTime?.toString())
                    root.eventDuration.setText(this.duration?.toString())
                    root.eventViewName.setText(this.viewName)
                    root.eventMeta.setText(this.meta?.toString())
                    root.eventBackendTracingId.setText(this.backendTracingId)
                    root.eventError.setText(this.error)
                    listenEditTextChanges(root, true)
                }
            }
        }

        // EditTexts
        listenEditTextChanges(root, true)

        // Run Button
        root.sendEvent.setOnClickListener {
            waitingResponse = true
            viewModel.sendCustomEvent(
                eventName = root.eventName.text.toString(),
                startTimeEpochMs = root.eventStartTime.text?.toString()?.notBlankOrNull()?.toLong(),
                durationMs = root.eventDuration.text?.toString()?.notBlankOrNull()?.toLong(),
                viewName = root.eventViewName.text?.toString()?.notBlankOrNull(),
                meta = root.eventMeta.text?.toString()?.notBlankOrNull()?.toMap(),
                backendTracingID = root.eventBackendTracingId.text?.toString()?.notBlankOrNull(),
                errorMessage = root.eventError.text?.toString()?.notBlankOrNull()
            )
        }

        return root
    }

    private fun listenEditTextChanges(root: View, enabled: Boolean) {
        val textInputs = listOf(
            root.eventName,
            root.eventStartTime,
            root.eventDuration,
            root.eventViewName,
            root.eventMeta,
            root.eventBackendTracingId,
            root.eventError
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
                        root.eventPresets.setSelection(presets.size)
                    }
                }
            }
            textInputs.forEach { it.addTextChangedListener(textChangeListener) }
        }
    }
}
