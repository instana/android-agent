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
import com.instana.mobileeum.R
import com.instana.mobileeum.network.RequestPreset
import kotlinx.android.synthetic.main.fragment_httpurlconnection.view.*

class HttpURLConnectionFragment : Fragment() {

    private lateinit var httpURLConnectionViewModel: HttpURLConnectionViewModel

    private var presets = emptyList<RequestPreset>()
    private var waitingResponse = false

    private var textChangeListener: TextWatcher? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        httpURLConnectionViewModel = ViewModelProvider(this).get(HttpURLConnectionViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_httpurlconnection, container, false)

        // Request Presets
        httpURLConnectionViewModel.presets.observe(viewLifecycleOwner, Observer { list ->
            presets = list
            val spinnerOptions = list.map { it.name }.toMutableList().apply { add("CUSTOM") }
            root.requestPresets.adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, spinnerOptions)
        })
        root.requestPresets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                presets.getOrNull(position)?.run {
                    listenEditTextChanges(root, false)
                    root.requestMethod.setText(this.method)
                    root.requestUrl.setText(this.url)
                    root.requestBody.setText(this.body)
                    listenEditTextChanges(root, true)
                }
            }
        }

        // EditTexts
        listenEditTextChanges(root, true)

        // Run Button
        root.runRequest.setOnClickListener {
            waitingResponse = true
            httpURLConnectionViewModel.executeRequest(
                method = root.requestMethod.text.toString(),
                url = root.requestUrl.text.toString(),
                body = if (root.requestBody.text.isNullOrBlank()) null else root.requestBody.text.toString()
            )
        }

        // Response
        httpURLConnectionViewModel.response.observe(viewLifecycleOwner, Observer {
            root.response.text = it
            if (waitingResponse) root.scrollView.fullScroll(View.FOCUS_DOWN)
        })

        return root
    }

    private fun listenEditTextChanges(root: View, enabled: Boolean) {
        textChangeListener?.run {
            root.requestMethod.removeTextChangedListener(this)
            root.requestUrl.removeTextChangedListener(this)
            root.requestBody.removeTextChangedListener(this)
        }
        if (enabled) {
            textChangeListener = object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (root.requestMethod.isFocused || root.requestUrl.isFocused || root.requestBody.isFocused) {
                        root.requestPresets.setSelection(presets.size)
                    }
                }
            }
            root.requestMethod.addTextChangedListener(textChangeListener)
            root.requestUrl.addTextChangedListener(textChangeListener)
            root.requestBody.addTextChangedListener(textChangeListener)
        }
    }
}
