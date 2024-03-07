package com.example.integration1

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class AddDataFragment : Fragment() {
    private lateinit var simplifiedDescription: String
    private lateinit var amount: EditText
    private lateinit var description: EditText
    private lateinit var date: EditText
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private lateinit var id: String
    private lateinit var userName: String
    private lateinit var roomId: String

    private val contextTAG : String = "AddDataFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_add_data, container, false)
        val uploadData = v.findViewById<Button>(R.id.uploadid)
        val clearData = v.findViewById<Button>(R.id.clear_id)

        id = userDataViewModel.userId
        userName = userDataViewModel.userName
        roomId = userDataViewModel.roomId
        amount = v.findViewById(R.id.amountid)
        description = v.findViewById(R.id.descriptionid)
        date = v.findViewById(R.id.dateid)

        val dateTil = v.findViewById<TextInputLayout>(R.id.date_til)
        val amountTil = v.findViewById<TextInputLayout>(R.id.amount_til)
        val descriptionTil = v.findViewById<TextInputLayout>(R.id.description_til)
        dateTil.setStartIconTintList(null)
        amountTil.setStartIconTintList(null)
        descriptionTil.setStartIconTintList(null)
        val calendar = Calendar.getInstance()
        val year1 = calendar[Calendar.YEAR]
        val month1 = calendar[Calendar.MONTH]
        val date1 = calendar[Calendar.DAY_OF_MONTH]

        date.requestFocus()
        date.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { _, year, month, day ->
                    val monthVar = month + 1
                    val date2 = String.format("%02d/%02d/%04d", day, monthVar, year)
                    date.error = null
                    date.setText(date2)
                }, year1, month1, date1
            )
            datePickerDialog.show()
        }

        uploadData.setOnClickListener {
            simplifiedDescription = description.text.toString().trim { it <= ' ' }
            if (date.text.toString().trim { it <= ' ' }.length < 10 || date.text.toString()
                    .trim { it <= ' ' }.length > 10
            ) {
                date.error = "Date wrongly formatted"
            } else if (!date.text.toString().trim { it <= ' ' }.contains("/")) {
                date.error = "Date format DD/MM/YYYY"
            } else if (amount.text.toString().trim { it <= ' ' }.isEmpty()) {
                amount.error = "Amount should not be empty"
            } else if (simplifiedDescription.isEmpty()) {
                description.error = "Description should not be empty"
            } else {
                upload()
            }
        }

        clearData.setOnClickListener {
            date.setText("")
            amount.setText("")
            description.setText("")
        }



        return v
    }

    private fun dateFormat(dateString: String): String {
        // Split the date string into day, month, and year components
        val dateComponents = dateString.split("/".toRegex())
        val day = dateComponents[0]
        val month = dateComponents[1]
        val year = dateComponents[2]
        // Return the date string in "MM/DD/YYYY" format
        return "$month/$day/$year"
    }


    private fun upload() {
        val mBuilder = AlertDialog.Builder(requireActivity())
        val view1: View = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        val dateD = view1.findViewById<TextView>(R.id.date_confirm_id)
        val amountD = view1.findViewById<TextView>(R.id.amount_confirm_id)
        val descriptionD = view1.findViewById<TextView>(R.id.description_confirm_id)
        val cancel = view1.findViewById<Button>(R.id.cancel_confirm_id)
        val upload = view1.findViewById<Button>(R.id.confirm_confirm_id)
        val ll1 = view1.findViewById<LinearLayout>(R.id.ll1)
        val animationView = view1.findViewById<LottieAnimationView>(R.id.lottie_animation_1)
        mBuilder.setView(view1)
        val dialog1 = mBuilder.create()
        val dateVal = date.text.toString().trim { it <= ' ' }
        val amountVal = amount.text.toString().trim { it <= ' ' }
        val descriptionVal = description.text.toString()

        Log.i(contextTAG, "date = $dateVal")
        dateD.text = dateVal
        amountD.text = getString(R.string.amount_entered_AD, amountVal)
        descriptionD.text = getString(R.string.description_entered_AD, descriptionVal)
        dialog1.setCanceledOnTouchOutside(false)
        upload.setOnClickListener {
            val url = resources.getString(R.string.spreadsheet_url)
            ll1.visibility = View.GONE
            animationView.visibility = View.VISIBLE
            animationView.setAnimation(R.raw.meditation_wait_please)
            animationView.playAnimation()
            val stringRequest: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->
                        animationView.setAnimation(R.raw.done)
                        animationView.playAnimation()
                        Toast.makeText(
                            requireActivity().applicationContext,
                            response,
                            Toast.LENGTH_SHORT
                        ).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            dialog1.dismiss()
                            amount.setText("")
                            description.setText("")
                            date.setText("")
                        }, 2000)
                    },
                    Response.ErrorListener {
                        animationView.setAnimation(R.raw.error)
                        animationView.playAnimation()
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        return hashMapOf(
                            "action" to "addItem",
                            "userId" to id,
                            "roomId" to roomId,
                            "userName" to userName,
                            "date" to dateFormat(dateVal),
                            "amount" to amountVal,
                            "description" to descriptionVal,
                            "foodId" to "0",
                            "profileId" to userDataViewModel.profileId
                        )
                    }
                }
            val socketTimeOut = 50000 // u can change this .. here it is 50 seconds
            val retryPolicy: RetryPolicy =
                DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            stringRequest.setRetryPolicy(retryPolicy)
            val queue = Volley.newRequestQueue(requireActivity().applicationContext)
            queue.add(stringRequest)
            date.setText("")
            amount.setText("")
            description.setText("")

        }
        cancel.setOnClickListener { dialog1.dismiss() }
        dialog1.show()
    }

}