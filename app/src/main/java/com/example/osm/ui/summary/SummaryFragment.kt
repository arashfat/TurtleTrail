package com.example.osm.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.osm.databinding.FragmentSummaryBinding
import com.example.osm.model.Summary
import com.example.osm.ui.mapUtils.convertSecondToText
import com.example.osm.ui.mapUtils.formatDistanceText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SummaryFragment: BottomSheetDialogFragment() {
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private var summary: Summary? = null
    private var onClick:(() -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setData(summary: Summary, onClick:() -> Unit) {
        this.summary = summary
        this.onClick = onClick
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDistance.text = formatDistanceText(summary?.distance?.toInt()!!)
        binding.tvTravelTime.text = convertSecondToText(summary?.duration?.toInt()!!)

        binding.btnStart.setOnClickListener {
            onClick?.invoke()
            dismiss()
        }
    }
}