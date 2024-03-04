package com.example.integration1

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment


class DefaultFragment : Fragment() {

    private val contextTAG : String = "DefaultFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.i(contextTAG, " OnCreateView")

        return inflater.inflate(R.layout.fragment_default, container, false)
    }

}