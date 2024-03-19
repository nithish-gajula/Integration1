package com.example.integration1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement


class DefaultFragment : Fragment() {

    private val contextTAG : String = "DefaultFragment"
    private lateinit var welcome : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_default, container, false)

        welcome = v.findViewById(R.id.welcome)
        welcome.setOnClickListener { ActivityUtils.navigateToActivity(requireActivity(), Intent(requireActivity(), FullViewChart::class.java)) }

        return v
    }




}